import React, { useEffect, useState, useMemo } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { preceptorService, type PreceptorSearchItem } from '../../services/preceptor';

const PublicBrowse: React.FC = () => {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const initialSpecialty = searchParams.get('specialty') || '';

  const [preceptors, setPreceptors] = useState<PreceptorSearchItem[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedSpecialty, setSelectedSpecialty] = useState(initialSpecialty);
  const [selectedLocation, setSelectedLocation] = useState('');

  const fetchPreceptors = async () => {
    try {
      setIsLoading(true);
      const result = await preceptorService.searchPreceptors({
        specialty: selectedSpecialty || undefined,
        location: selectedLocation || undefined,
        size: 50
      });
      setPreceptors(result.items);
    } catch (error) {
      console.error('Failed to fetch preceptors:', error);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchPreceptors();
  }, [selectedSpecialty]);

  const filteredPreceptors = useMemo(() => {
    return preceptors.filter(p => {
      const matchSearch = !searchQuery || 
        p.displayName.toLowerCase().includes(searchQuery.toLowerCase()) ||
        (p.specialty?.toLowerCase().includes(searchQuery.toLowerCase()));
      const matchLocation = !selectedLocation || 
        p.location?.toLowerCase().includes(selectedLocation.toLowerCase());
      return matchSearch && matchLocation;
    });
  }, [preceptors, searchQuery, selectedLocation]);

  const handleInquiry = () => {
    const token = localStorage.getItem('accessToken');
    if (!token) {
      navigate('/login?redirect=/browse');
    } else {
      // If logged in, they should ideally be in the student portal, 
      // but for public page we can just redirect them to their dashboard
      const role = localStorage.getItem('role');
      if (role === 'STUDENT') navigate('/student/dashboard');
      else if (role === 'PRECEPTOR') navigate('/preceptor/dashboard');
      else navigate('/login');
    }
  };

  return (
    <div className="min-h-screen bg-slate-50 font-sans text-slate-800">
      {/* Header */}
      <nav className="sticky top-0 z-50 bg-white/80 backdrop-blur-md border-b border-slate-200">
        <div className="flex items-center justify-between px-6 py-4 mx-auto max-w-7xl lg:px-8">
          <div className="flex items-center gap-3 cursor-pointer" onClick={() => navigate('/')}>
            <div className="flex items-center justify-center w-10 h-10 text-white rounded-full bg-cyan-500 shadow-md transition-transform hover:scale-105">
              <span className="text-xl font-bold material-symbols-outlined">health_and_safety</span>
            </div>
            <span className="text-2xl font-black tracking-tight text-slate-900">NPaxis</span>
          </div>
          <div className="flex items-center gap-4">
             <button onClick={() => navigate('/login')} className="px-5 py-2 text-sm font-bold text-slate-600 hover:text-cyan-600 transition-colors">Sign In</button>
             <button onClick={() => navigate('/register')} className="px-5 py-2 text-sm font-bold text-white bg-cyan-500 rounded-full shadow-lg hover:bg-cyan-600 transition-all">Join Now</button>
          </div>
        </div>
      </nav>

      {/* Hero / Filter Bar */}
      <div className="bg-white border-b border-slate-200">
        <div className="px-6 py-8 mx-auto max-w-7xl lg:px-8">
          <h1 className="text-3xl font-black text-slate-900 mb-6">Browse Verified Preceptors</h1>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            <div className="relative">
              <span className="absolute left-4 top-1/2 -translate-y-1/2 material-symbols-outlined text-slate-400">search</span>
              <input 
                type="text" 
                placeholder="Search name or keywords..." 
                className="w-full pl-12 pr-4 py-3 bg-slate-50 border border-slate-200 rounded-2xl outline-none focus:ring-4 focus:ring-cyan-500/10 focus:border-cyan-500 transition-all"
                value={searchQuery}
                onChange={e => setSearchQuery(e.target.value)}
              />
            </div>
            <div className="relative">
              <span className="absolute left-4 top-1/2 -translate-y-1/2 material-symbols-outlined text-slate-400">medical_services</span>
              <select 
                className="w-full pl-12 pr-4 py-3 bg-slate-50 border border-slate-200 rounded-2xl outline-none focus:ring-4 focus:ring-cyan-500/10 focus:border-cyan-500 appearance-none"
                value={selectedSpecialty}
                onChange={e => setSelectedSpecialty(e.target.value)}
              >
                <option value="">All Specialties</option>
                <option value="Primary Care">Primary Care</option>
                <option value="Pediatric">Pediatric</option>
                <option value="Family Medicine">Family Medicine</option>
                <option value="Emergency">Emergency</option>
                <option value="Orthopaedic">Orthopaedic</option>
              </select>
            </div>
            <div className="relative">
              <span className="absolute left-4 top-1/2 -translate-y-1/2 material-symbols-outlined text-slate-400">location_on</span>
              <input 
                type="text" 
                placeholder="Filter by city/state..." 
                className="w-full pl-12 pr-4 py-3 bg-slate-50 border border-slate-200 rounded-2xl outline-none focus:ring-4 focus:ring-cyan-500/10 focus:border-cyan-500 transition-all"
                value={selectedLocation}
                onChange={e => setSelectedLocation(e.target.value)}
              />
            </div>
          </div>
        </div>
      </div>

      {/* Listings */}
      <div className="px-6 py-12 mx-auto max-w-7xl lg:px-8">
        {isLoading ? (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
            {Array.from({ length: 9 }).map((_, i) => (
              <div key={i} className="h-64 bg-white border border-slate-200 rounded-3xl animate-pulse" />
            ))}
          </div>
        ) : filteredPreceptors.length > 0 ? (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
            {filteredPreceptors.map(doc => (
              <div key={doc.userId} className="group relative bg-white border border-slate-100 p-6 rounded-[2rem] shadow-sm hover:shadow-xl transition-all duration-300 ring-1 ring-slate-200/50">
                <div className="flex items-start justify-between mb-6">
                  <div className="w-16 h-16 bg-cyan-50 rounded-2xl flex items-center justify-center text-cyan-500 text-3xl font-black">
                    <span className="material-symbols-outlined text-4xl">person</span>
                  </div>
                  <div className="flex flex-col items-end">
                    <span className={`px-3 py-1 text-[10px] font-black uppercase tracking-widest rounded-full ${doc.isVerified ? 'bg-emerald-100 text-emerald-600' : 'bg-amber-100 text-amber-600'}`}>
                      {doc.isVerified ? 'Verified' : 'Pending'}
                    </span>
                    {doc.isPremium && (
                      <span className="mt-1 px-3 py-1 text-[10px] font-black uppercase tracking-widest bg-blue-100 text-blue-600 rounded-full">Premium</span>
                    )}
                  </div>
                </div>

                <h3 className="text-xl font-black text-slate-900 truncate">{doc.displayName}</h3>
                <p className="text-xs font-bold text-cyan-600 uppercase tracking-wide mt-1">{doc.specialty || 'General Practice'} • {doc.credentials}</p>
                
                <div className="mt-6 space-y-3">
                  <div className="flex items-center gap-3 text-sm text-slate-500 font-medium">
                    <span className="material-symbols-outlined text-slate-400">location_on</span>
                    {doc.location || 'Location Not Specified'}
                  </div>
                  <div className="flex items-center gap-3 text-sm text-slate-500 font-medium">
                    <span className="material-symbols-outlined text-slate-400">payments</span>
                    {doc.honorarium || 'Contact for Details'}
                  </div>
                </div>

                <div className="mt-8 flex gap-3">
                  <button 
                    onClick={handleInquiry}
                    className="flex-1 py-3 bg-[#39b54a] text-white font-bold rounded-2xl shadow-lg shadow-emerald-500/20 hover:bg-[#2d9e3d] transition-all flex items-center justify-center gap-2"
                  >
                    Send Inquiry
                    <span className="material-symbols-outlined text-sm">chat</span>
                  </button>
                  <button 
                    onClick={handleInquiry}
                    className="w-12 h-12 bg-slate-50 border border-slate-100 text-slate-400 rounded-2xl flex items-center justify-center hover:bg-cyan-50 hover:text-cyan-500 transition-all"
                  >
                    <span className="material-symbols-outlined">favorite</span>
                  </button>
                </div>
              </div>
            ))}
          </div>
        ) : (
          <div className="py-20 text-center space-y-4">
             <div className="w-20 h-20 bg-slate-100 text-slate-400 rounded-full flex items-center justify-center mx-auto text-4xl">
               <span className="material-symbols-outlined text-[48px]">search_off</span>
             </div>
             <h2 className="text-2xl font-black text-slate-800">No Preceptors Found</h2>
             <p className="text-slate-500">Try adjusting your filters or searching for something else.</p>
             <button onClick={() => {setSelectedSpecialty(''); setSearchQuery('');}} className="px-6 py-2 bg-cyan-500 text-white font-bold rounded-full">Reset Filters</button>
          </div>
        )}
      </div>

      {/* Footer */}
      <footer className="bg-white border-t border-slate-200 py-12">
        <div className="px-6 mx-auto max-w-7xl lg:px-8 text-center">
          <p className="text-sm font-semibold text-slate-400 tracking-wide uppercase">&copy; {new Date().getFullYear()} NPaxis Platform. All rights reserved.</p>
        </div>
      </footer>
    </div>
  );
};

export default PublicBrowse;
