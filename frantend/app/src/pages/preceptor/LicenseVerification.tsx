import React, { useEffect, useMemo, useState } from 'react';
import PreceptorLayout from '../../components/layout/PreceptorLayout';
import { preceptorService, type VerificationStatus } from '../../services/preceptor';

type AlertType = 'success' | 'error';

const LicenseVerification: React.FC = () => {
  const preceptorId = useMemo(() => localStorage.getItem('userId'), []);
  const [licenseNumber, setLicenseNumber] = useState('');
  const [licenseState, setLicenseState] = useState('');
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [previewUrl, setPreviewUrl] = useState<string | null>(null);
  const [uploadProgress, setUploadProgress] = useState(0);
  const [isUploading, setIsUploading] = useState(false);
  const [isLoadingStatus, setIsLoadingStatus] = useState(true);
  const [verificationStatus, setVerificationStatus] = useState<VerificationStatus | null>(null);
  const [alert, setAlert] = useState<{ type: AlertType; text: string } | null>(null);

  useEffect(() => {
    if (!preceptorId) {
      setIsLoadingStatus(false);
      setAlert({ type: 'error', text: 'Preceptor ID is missing. Please login again.' });
      return;
    }

    const loadCurrentStatus = async () => {
      try {
        const profile = await preceptorService.getPreceptorById(preceptorId);
        setVerificationStatus(profile.verificationStatus ?? null);
        setLicenseNumber(profile.licenseNumber ?? '');
        setLicenseState(profile.licenseState ?? '');
      } catch (err: any) {
        setAlert({ type: 'error', text: err.message || 'Unable to load verification status.' });
      } finally {
        setIsLoadingStatus(false);
      }
    };

    loadCurrentStatus();
  }, [preceptorId]);

  useEffect(() => {
    return () => {
      if (previewUrl) {
        URL.revokeObjectURL(previewUrl);
      }
    };
  }, [previewUrl]);

  const status = (verificationStatus || 'NOT_SUBMITTED').toUpperCase();
  const statusStyles =
    status === 'APPROVED'
      ? 'bg-emerald-100 text-emerald-700'
      : status === 'REJECTED'
      ? 'bg-red-100 text-red-700'
      : status === 'PENDING'
      ? 'bg-amber-100 text-amber-700'
      : 'bg-slate-100 text-slate-700';

  const isPreviewImage = selectedFile?.type.startsWith('image/');
  const isPreviewPdf = selectedFile?.type === 'application/pdf';

  const handleFileChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0] ?? null;
    setAlert(null);
    setUploadProgress(0);
    setSelectedFile(file);

    if (previewUrl) {
      URL.revokeObjectURL(previewUrl);
      setPreviewUrl(null);
    }

    if (file && (file.type.startsWith('image/') || file.type === 'application/pdf')) {
      setPreviewUrl(URL.createObjectURL(file));
    }
  };

  const handleUpload = async () => {
    if (!preceptorId) {
      setAlert({ type: 'error', text: 'Preceptor ID is missing. Please login again.' });
      return;
    }

    if (!selectedFile) {
      setAlert({ type: 'error', text: 'Please choose a file before uploading.' });
      return;
    }

    try {
      setAlert(null);
      setIsUploading(true);
      setUploadProgress(0);

      const updatedProfile = await preceptorService.submitLicense(
        preceptorId,
        {
          file: selectedFile,
          licenseNumber: licenseNumber.trim(),
          licenseState: licenseState.trim(),
        },
        (progressEvent) => {
          const total = progressEvent.total || selectedFile.size;
          if (!total) return;
          setUploadProgress(Math.min(100, Math.round((progressEvent.loaded * 100) / total)));
        }
      );

      setVerificationStatus(updatedProfile.verificationStatus ?? 'PENDING');
      setAlert({ type: 'success', text: 'License uploaded successfully. Verification is now pending.' });
    } catch (err: any) {
      setAlert({ type: 'error', text: err.message || 'Upload failed. Please try again.' });
    } finally {
      setIsUploading(false);
    }
  };

  return (
    <PreceptorLayout>
      <div className="max-w-4xl mx-auto">
        <div className="mb-8">
          <h2 className="text-3xl font-extrabold font-headline tracking-tight text-on-surface mb-2">License Verification</h2>
          <p className="text-slate-500 font-medium">Upload your license document to start or update verification.</p>
        </div>

        <div className="bg-white border border-slate-200 rounded-2xl p-6 shadow-sm">
          <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 mb-6">
            <div>
              <h3 className="text-lg font-bold text-slate-900">Verification Status</h3>
              <p className="text-sm text-slate-500">Current review state of your submitted license.</p>
            </div>
            <span className={`inline-flex items-center rounded-full px-3 py-1 text-xs font-bold tracking-wide ${statusStyles}`}>
              {isLoadingStatus ? 'LOADING' : status.replace('_', ' ')}
            </span>
          </div>

          {alert && (
            <div
              className={`mb-6 rounded-lg px-4 py-3 text-sm font-medium ${
                alert.type === 'success'
                  ? 'bg-emerald-50 text-emerald-700 border border-emerald-200'
                  : 'bg-red-50 text-red-700 border border-red-200'
              }`}
            >
              {alert.text}
            </div>
          )}

          <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mb-6">
            <div className="space-y-2">
              <label className="block text-xs font-semibold uppercase tracking-wider text-slate-500">License Number</label>
              <input
                type="text"
                value={licenseNumber}
                onChange={(e) => setLicenseNumber(e.target.value)}
                placeholder="Enter license number"
                className="w-full rounded-lg border border-slate-200 bg-slate-50 px-4 py-3 text-sm text-slate-800 focus:outline-none focus:ring-2 focus:ring-blue-600"
              />
            </div>
            <div className="space-y-2">
              <label className="block text-xs font-semibold uppercase tracking-wider text-slate-500">License State</label>
              <input
                type="text"
                value={licenseState}
                onChange={(e) => setLicenseState(e.target.value)}
                placeholder="e.g. CA, TX, NY"
                className="w-full rounded-lg border border-slate-200 bg-slate-50 px-4 py-3 text-sm text-slate-800 focus:outline-none focus:ring-2 focus:ring-blue-600"
              />
            </div>
          </div>

          <div className="mb-4">
            <label className="block text-xs font-semibold uppercase tracking-wider text-slate-500 mb-2">License File</label>
            <input
              type="file"
              accept=".pdf,.png,.jpg,.jpeg,image/png,image/jpeg,application/pdf"
              onChange={handleFileChange}
              className="w-full rounded-lg border border-slate-200 bg-slate-50 px-4 py-3 text-sm text-slate-700 file:mr-4 file:rounded-md file:border-0 file:bg-blue-600 file:px-3 file:py-2 file:text-white file:text-sm file:font-semibold hover:file:opacity-90"
            />
            <p className="mt-2 text-xs text-slate-500">Accepted: PDF, PNG, JPG, JPEG</p>
          </div>

          {selectedFile && (
            <div className="mb-6 rounded-lg border border-slate-200 bg-slate-50 p-4">
              <p className="text-sm font-semibold text-slate-800 mb-1">{selectedFile.name}</p>
              <p className="text-xs text-slate-500">{(selectedFile.size / (1024 * 1024)).toFixed(2)} MB</p>
            </div>
          )}

          {previewUrl && (
            <div className="mb-6">
              <p className="text-xs font-semibold uppercase tracking-wider text-slate-500 mb-2">File Preview</p>
              <div className="rounded-xl border border-slate-200 bg-slate-50 p-3">
                {isPreviewImage && <img src={previewUrl} alt="License preview" className="max-h-96 w-auto rounded-lg" />}
                {isPreviewPdf && (
                  <object data={previewUrl} type="application/pdf" className="h-96 w-full rounded-lg">
                    <p className="text-sm text-slate-600">Preview unavailable. Please open the PDF after upload.</p>
                  </object>
                )}
              </div>
            </div>
          )}

          {(isUploading || uploadProgress > 0) && (
            <div className="mb-6">
              <div className="mb-2 flex items-center justify-between text-sm font-medium text-slate-600">
                <span>Upload Progress</span>
                <span>{uploadProgress}%</span>
              </div>
              <div className="h-2 rounded-full bg-slate-200">
                <div
                  className="h-2 rounded-full bg-blue-600 transition-all duration-150"
                  style={{ width: `${uploadProgress}%` }}
                />
              </div>
            </div>
          )}

          <button
            type="button"
            onClick={handleUpload}
            disabled={isUploading || !selectedFile || !preceptorId}
            className="inline-flex items-center gap-2 rounded-full bg-blue-700 px-6 py-3 text-sm font-bold text-white shadow-md transition-opacity hover:opacity-90 disabled:cursor-not-allowed disabled:opacity-50"
          >
            {isUploading ? (
              <>
                <span className="material-symbols-outlined animate-spin text-sm">progress_activity</span>
                Uploading...
              </>
            ) : (
              <>
                <span className="material-symbols-outlined text-sm">upload</span>
                Upload License
              </>
            )}
          </button>
        </div>
      </div>
    </PreceptorLayout>
  );
};

export default LicenseVerification;
