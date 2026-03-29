import React, { useRef, useState } from 'react';

interface FileUploadProps {
  file: File | null;
  previewUrl: string | null;
  disabled?: boolean;
  onFileSelect: (file: File | null) => void;
}

const formatFileSize = (size: number) => {
  if (size < 1024) return `${size} B`;
  if (size < 1024 * 1024) return `${(size / 1024).toFixed(1)} KB`;
  return `${(size / (1024 * 1024)).toFixed(2)} MB`;
};

const FileUpload: React.FC<FileUploadProps> = ({ file, previewUrl, disabled = false, onFileSelect }) => {
  const inputRef = useRef<HTMLInputElement | null>(null);
  const [isDragOver, setIsDragOver] = useState(false);

  const openFilePicker = () => {
    if (disabled) return;
    inputRef.current?.click();
  };

  const onDropFile = (event: React.DragEvent<HTMLDivElement>) => {
    event.preventDefault();
    if (disabled) return;
    setIsDragOver(false);
    const droppedFile = event.dataTransfer.files?.[0] ?? null;
    onFileSelect(droppedFile);
  };

  return (
    <div className="space-y-3">
      <label className="block text-xs font-bold uppercase tracking-wider text-slate-500">License Document</label>

      <div
        onDrop={onDropFile}
        onDragOver={(event) => {
          event.preventDefault();
          if (!disabled) setIsDragOver(true);
        }}
        onDragLeave={() => setIsDragOver(false)}
        className={`rounded-2xl border-2 border-dashed p-6 text-center transition-all ${
          isDragOver ? 'border-blue-400 bg-blue-50' : 'border-slate-300 bg-slate-50'
        }`}
      >
        <input
          ref={inputRef}
          type="file"
          className="hidden"
          accept=".pdf,.png,.jpg,.jpeg,application/pdf,image/png,image/jpeg"
          onChange={(event) => onFileSelect(event.target.files?.[0] ?? null)}
          disabled={disabled}
        />

        <div className="mx-auto mb-3 inline-flex h-12 w-12 items-center justify-center rounded-full bg-blue-100 text-blue-700">
          <span className="material-symbols-outlined">upload_file</span>
        </div>
        <p className="text-sm font-semibold text-slate-700">Drag and drop your file here</p>
        <p className="mt-1 text-xs text-slate-500">PDF, JPG, or PNG up to 5MB</p>
        <button
          type="button"
          onClick={openFilePicker}
          disabled={disabled}
          className="mt-4 rounded-full bg-blue-700 px-4 py-2 text-xs font-bold text-white hover:bg-blue-800 disabled:opacity-60"
        >
          Browse Files
        </button>
      </div>

      {file ? (
        <div className="rounded-xl border border-slate-200 bg-white p-3">
          <p className="text-sm font-semibold text-slate-800">{file.name}</p>
          <p className="text-xs text-slate-500">{formatFileSize(file.size)}</p>
        </div>
      ) : null}

      {previewUrl ? (
        <div className="rounded-xl border border-slate-200 bg-white p-3">
          <p className="mb-2 text-xs font-bold uppercase tracking-wider text-slate-500">Preview</p>
          <img src={previewUrl} alt="License preview" className="max-h-72 rounded-lg object-contain" />
        </div>
      ) : null}
    </div>
  );
};

export default FileUpload;
