import React, { useEffect, useRef, useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { preceptorService, type PreceptorSearchItem } from '../../services/preceptor';

const ParticleBackground: React.FC<{ className?: string }> = ({ className }) => {
  const canvasRef = useRef<HTMLCanvasElement>(null);

  useEffect(() => {
    const canvas = canvasRef.current;
    if (!canvas) return;
    const parent = canvas.parentElement;
    if (!parent) return;
    
    const ctx = canvas.getContext('2d');
    if (!ctx) return;

    let w = canvas.width = parent.clientWidth;
    let h = canvas.height = parent.clientHeight;
    
    // Resize handler
    const resize = () => {
      w = canvas.width = parent.clientWidth;
      h = canvas.height = parent.clientHeight;
    };
    window.addEventListener('resize', resize);

    // Particle class
    class Particle {
      x: number;
      y: number;
      z: number;
      vx: number;
      vy: number;
      size: number;
      color: string;
      
      constructor() {
        this.x = Math.random() * w;
        this.y = Math.random() * h;
        this.z = Math.random() * 2;
        this.vx = (Math.random() - 0.5) * 0.5;
        this.vy = (Math.random() - 0.5) * 0.5;
        this.size = Math.random() * 3 + 1;
        
        // Random cyan to teal medical colors
        const colors = ['rgba(6,182,212,0.8)', 'rgba(56,189,248,0.6)', 'rgba(16,185,129,0.5)'];
        this.color = colors[Math.floor(Math.random() * colors.length)];
      }

      update(mouseX: number, mouseY: number) {
        this.x += this.vx * (1 + this.z);
        this.y += this.vy * (1 + this.z);

        if (this.x < 0 || this.x > w) this.vx *= -1;
        if (this.y < 0 || this.y > h) this.vy *= -1;

        // Subtle mouse interaction (gravity)
        const dx = mouseX - this.x;
        const dy = mouseY - this.y;
        const dist = Math.sqrt(dx * dx + dy * dy);
        if (dist < 150) {
          this.x -= dx * 0.01;
          this.y -= dy * 0.01;
        }
      }

      draw() {
        if (!ctx) return;
        ctx.beginPath();
        ctx.arc(this.x, this.y, this.size * this.z, 0, Math.PI * 2);
        ctx.fillStyle = this.color;
        ctx.shadowBlur = 15;
        ctx.shadowColor = this.color;
        ctx.fill();
        ctx.shadowBlur = 0;
      }
    }

    const particles: Particle[] = Array.from({ length: 60 }, () => new Particle());
    let mouse = { x: -1000, y: -1000 };

    const handleMouseMove = (e: MouseEvent) => {
      const rect = canvas.getBoundingClientRect();
      mouse.x = e.clientX - rect.left;
      mouse.y = e.clientY - rect.top;
    };
    window.addEventListener('mousemove', handleMouseMove);

    let animationFrameId: number;
    const animate = () => {
      ctx.clearRect(0, 0, w, h);
      
      // Draw connections
      for (let i = 0; i < particles.length; i++) {
        for (let j = i + 1; j < particles.length; j++) {
          const dx = particles[i].x - particles[j].x;
          const dy = particles[i].y - particles[j].y;
          const dist = Math.sqrt(dx*dx + dy*dy);
          
          if (dist < 150) {
            ctx.beginPath();
            ctx.moveTo(particles[i].x, particles[i].y);
            ctx.lineTo(particles[j].x, particles[j].y);
            ctx.strokeStyle = `rgba(6,182,212,${0.15 - dist/1000})`;
            ctx.lineWidth = 1;
            ctx.stroke();
          }
        }
      }

      particles.forEach(p => {
        p.update(mouse.x, mouse.y);
        p.draw();
      });

      animationFrameId = requestAnimationFrame(animate);
    };

    animate();

    return () => {
      window.removeEventListener('resize', resize);
      window.removeEventListener('mousemove', handleMouseMove);
      cancelAnimationFrame(animationFrameId);
    };
  }, []);

  return (
    <canvas 
      ref={canvasRef} 
      className={className || "absolute top-0 left-0 w-full h-full -z-10 pointer-events-none"}
    />
  );
};

const Landing: React.FC = () => {
  const navigate = useNavigate();
  const [preceptors, setPreceptors] = useState<PreceptorSearchItem[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [activeSpecialty, setActiveSpecialty] = useState('All');
  const [searchQuery, setSearchQuery] = useState('');

  const fetchPreceptors = async (query?: string) => {
    try {
      setIsLoading(true);
      const result = await preceptorService.searchPreceptors({
        specialty: query || (activeSpecialty === 'All' ? undefined : activeSpecialty),
        size: 6
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
  }, [activeSpecialty]);

  const handleSearch = () => {
    if (searchQuery.trim()) {
      navigate(`/browse?specialty=${encodeURIComponent(searchQuery)}`);
    } else {
      navigate('/browse');
    }
  };

  return (
    <div className="relative min-h-screen bg-slate-50 font-sans text-slate-800 overflow-x-hidden pt-8 z-0">
      {/* 3D Background */}
      <ParticleBackground />
      <div className="absolute top-[-20%] right-[-10%] w-[80%] h-[1200px] bg-gradient-to-bl from-cyan-400/20 to-emerald-400/10 rounded-full blur-[120px] -z-20 pointer-events-none" />
      <div className="absolute top-[40%] left-[-10%] w-[50%] h-[800px] bg-gradient-to-tr from-blue-400/10 to-transparent rounded-full blur-[100px] -z-20 pointer-events-none" />

      {/* Navigation */}
      <nav className="relative z-50 flex items-center justify-between px-6 py-4 mx-auto max-w-7xl lg:px-8">
        <div className="flex items-center gap-3">
          <div className="flex items-center justify-center w-10 h-10 text-white rounded-full bg-gradient-to-br from-cyan-400 to-blue-500 shadow-md">
            <span className="text-xl font-bold material-symbols-outlined">health_and_safety</span>
          </div>
          <span className="text-2xl font-black tracking-tight text-slate-900">NPaxis</span>
        </div>
        <div className="hidden gap-8 md:flex">
          <button 
            onClick={() => window.scrollTo({ top: 0, behavior: 'smooth' })}
            className="font-semibold transition-colors text-cyan-600 border-b-2 border-cyan-500 pb-1"
          >
            Home
          </button>
          <button 
            onClick={() => navigate('/browse')}
            className="font-semibold text-slate-600 hover:text-cyan-600 transition-colors"
          >
            Browse Preceptors
          </button>
          <button 
            onClick={() => navigate('/about')}
            className="font-semibold text-slate-600 hover:text-cyan-600 transition-colors"
          >
            About
          </button>
          <button 
            onClick={() => navigate('/contact')}
            className="font-semibold text-slate-600 hover:text-cyan-600 transition-colors"
          >
            Contact Us
          </button>
        </div>
        <div className="flex items-center gap-4">
          <button 
            onClick={() => navigate('/login')}
            className="flex items-center justify-center w-10 h-10 transition-colors rounded-full text-slate-600 hover:bg-slate-100"
          >
            <span className="material-symbols-outlined">settings</span>
          </button>
          <button 
            onClick={() => navigate('/login')}
            className="hidden px-5 py-2 text-sm font-bold text-white transition-all rounded-full shadow-lg bg-cyan-500 hover:bg-cyan-600 md:block hover:shadow-cyan-500/25"
          >
            Sign In
          </button>
        </div>
      </nav>

      {/* Hero Section */}
      <section className="relative z-10 grid items-center grid-cols-1 gap-12 px-6 py-16 mx-auto max-w-7xl lg:grid-cols-2 lg:px-8 lg:py-24">
        <div className="max-w-xl">
          <h1 className="text-5xl font-extrabold tracking-tight leading-[1.15] text-slate-900 md:text-6xl">
            Trusted Preceptor <br /> Network
          </h1>
          <p className="mt-6 text-lg tracking-tight text-slate-600">
            NPaxis connects students with verified preceptors for real-world guidance, mentorship, and career acceleration. Find the perfect clinical placement today.
          </p>
          <div className="relative flex items-center max-w-md mt-10">
            <input 
              type="text" 
              placeholder="Type preceptor's specialty..." 
              className="w-full py-4 pl-6 pr-32 text-sm transition-all border outline-none rounded-full border-slate-200 focus:border-cyan-500 focus:ring-4 focus:ring-cyan-500/10 shadow-sm"
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              onKeyDown={(e) => e.key === 'Enter' && handleSearch()}
            />
            <button 
              onClick={handleSearch}
              className="absolute right-2 top-2 bottom-2 px-6 font-bold text-white transition-colors rounded-full bg-[#39b54a] hover:bg-[#2d9e3d]"
            >
              Search
            </button>
          </div>
        </div>
        <div className="relative flex justify-center lg:justify-end">
          <div className="relative w-full max-w-md overflow-hidden aspect-square rounded-[80px] rounded-tl-[160px] rounded-br-[160px] shadow-2xl border-8 border-white">
            <img 
              src="https://images.unsplash.com/photo-1559839734-2b71ea197ec2?auto=format&fit=crop&q=80&w=800" 
              alt="Medical Professional" 
              className="object-cover w-full h-full"
            />
          </div>
        </div>
      </section>

      {/* About Us */}
      <section id="about-section" className="relative py-20 overflow-hidden bg-white scroll-mt-10">
        <div className="absolute left-[-15%] top-1/2 -translate-y-1/2 w-[500px] h-[500px] bg-cyan-50 rounded-full -z-10" />
        
        <div className="grid items-center grid-cols-1 gap-16 px-6 mx-auto max-w-7xl lg:grid-cols-2 lg:px-8">
          <div className="relative flex justify-center">
            <div className="relative flex items-center justify-center w-72 h-72 rounded-full border-[12px] border-white shadow-xl bg-slate-100 overflow-hidden">
              <img src="https://images.unsplash.com/photo-1576091160550-2173ff9e5ee4?auto=format&fit=crop&q=80&w=800" className="object-cover w-full h-full opacity-60" alt="About Med" />
              <div className="absolute inset-0 flex items-center justify-center bg-cyan-900/40 backdrop-blur-[2px]">
                <h2 className="text-3xl font-black text-white drop-shadow-md">About Us</h2>
              </div>
            </div>
            {/* Decorative circles */}
            <div className="absolute top-0 right-10 w-4 h-4 rounded-full bg-[#39b54a]" />
            <div className="absolute bottom-10 left-10 w-8 h-8 rounded-full border-4 border-cyan-400" />
          </div>
          
          <div className="space-y-10">
            <div>
              <h2 className="text-3xl font-bold tracking-tight text-slate-800 hidden lg:block mb-10">About Us</h2>
              <div className="grid grid-cols-3 gap-8 text-center divide-x divide-slate-100">
                <div>
                  <p className="text-4xl font-black text-slate-800">+200</p>
                  <div className="inline-block px-3 py-1 mt-2 text-xs font-bold text-white rounded-full bg-[#39b54a]/80">Preceptor</div>
                </div>
                <div>
                  <p className="text-4xl font-black text-slate-800">+450</p>
                  <p className="mt-2 text-sm font-bold text-[#39b54a]">Student</p>
                </div>
                <div>
                  <p className="text-4xl font-black text-slate-800">24*7</p>
                  <p className="mt-2 text-sm font-semibold tracking-wide text-slate-500">Hours Open</p>
                </div>
              </div>
            </div>
            <p className="max-w-md pt-6 text-sm leading-relaxed border-t text-slate-500 border-slate-100">
              NPaxis connects students with verified preceptors to enable smarter learning, real mentorship, and faster career growth. We bridge the gap between academia and real-world clinical excellence.
            </p>
          </div>
        </div>
      </section>

      {/* Speciality */}
      <section className="relative py-24 bg-slate-50/50">
        <div className="absolute right-[-10%] top-0 w-[600px] h-[600px] bg-cyan-100/40 rounded-full blur-3xl -z-10" />
        
        <div className="grid items-center grid-cols-1 gap-16 px-6 mx-auto max-w-7xl lg:grid-cols-2 lg:px-8">
          {/* Circular Graphic Component */}
          <div className="relative flex items-center justify-center h-[500px]">
            {/* CSS Animation Injector */}
            <style dangerouslySetInnerHTML={{ __html: `
              @keyframes orbit-inner {
                from { transform: rotate(0deg); }
                to { transform: rotate(360deg); }
              }
              @keyframes orbit-outer {
                from { transform: rotate(360deg); }
                to { transform: rotate(0deg); }
              }
              @keyframes anti-spin {
                from { transform: rotate(360deg); }
                to { transform: rotate(0deg); }
              }
              @keyframes anti-spin-reverse {
                from { transform: rotate(0deg); }
                to { transform: rotate(360deg); }
              }
            `}} />

            {/* Main Center */}
            <div className="relative flex items-center justify-center z-20 w-48 h-48 text-center text-white rounded-full bg-cyan-500 shadow-[0_0_60px_rgba(6,182,212,0.5)]">
               <div className="absolute inset-0 bg-white opacity-20 rounded-full blur-xl animate-pulse" />
              <h2 className="relative text-2xl font-black leading-tight">Our<br/>Speciality</h2>
            </div>

            {/* Orbit rings & Icons */}
            <div className="absolute inset-0 flex items-center justify-center">
              {/* Inner Orbit (300px) */}
              <div className="absolute w-[300px] h-[300px] border border-cyan-200/50 rounded-full border-dashed" style={{ animation: 'orbit-inner 25s linear infinite' }}>
                {/* Icons on Inner Orbit */}
                <div className="absolute top-0 left-1/2 -ml-5 -mt-5 w-10 h-10 bg-cyan-500 rounded-full border-2 border-white shadow-lg flex items-center justify-center text-white" style={{ animation: 'anti-spin 25s linear infinite' }}>
                   <span className="text-[18px] material-symbols-outlined">medical_services</span>
                </div>
                <div className="absolute bottom-0 left-1/2 -ml-5 -mb-5 w-10 h-10 bg-[#39b54a] rounded-full border-2 border-white shadow-lg flex items-center justify-center text-white" style={{ animation: 'anti-spin 25s linear infinite' }}>
                   <span className="text-[18px] material-symbols-outlined">monitor_heart</span>
                </div>
              </div>

              {/* Outer Orbit (420px) */}
              <div className="absolute w-[420px] h-[420px] border border-slate-200/50 rounded-full" style={{ animation: 'orbit-outer 40s linear infinite' }}>
                {/* Icons on Outer Orbit */}
                <div className="absolute top-1/2 left-0 -ml-7 -mt-7 w-14 h-14 bg-[#ff4b72] rounded-full border-2 border-white shadow-lg flex items-center justify-center text-white" style={{ animation: 'anti-spin-reverse 40s linear infinite' }}>
                   <span className="text-[24px] material-symbols-outlined">ecg_heart</span>
                </div>
                <div className="absolute top-1/2 right-0 -mr-5 -mt-5 w-10 h-10 bg-amber-400 rounded-full border-2 border-white shadow-lg flex items-center justify-center text-white" style={{ animation: 'anti-spin-reverse 40s linear infinite' }}>
                   <span className="text-[18px] material-symbols-outlined">healing</span>
                </div>
                <div className="absolute top-0 left-1/2 -ml-5 -mt-5 w-10 h-10 bg-blue-500 rounded-full border-2 border-white shadow-lg flex items-center justify-center text-white" style={{ animation: 'anti-spin-reverse 40s linear infinite' }}>
                   <span className="text-[18px] material-symbols-outlined">psychology</span>
                </div>
              </div>
            </div>
          </div>
          
          <div className="space-y-8">
            <h2 className="text-4xl font-extrabold tracking-tight text-slate-800 lg:pr-10">
              Explore Top Clinical Specialties Today
            </h2>
            <p className="text-sm leading-relaxed text-slate-500">
              Browse thoroughly vetted preceptors across a myriad of nursing and medical specialties. Whether you need primary care exposure or highly specialized surgical rotations, our network provides the right match.
            </p>
            <div className="flex flex-wrap gap-3 mt-8">
              {['Dentist', 'Paediatric', 'Cardiology', 'Orthopaedic', 'Traumatology', 'Gynaecology', 'Neurology', 'Urology', 'Oncology'].map((spec, i) => (
                <span 
                  key={spec} 
                  className={`px-4 py-2 text-xs font-bold rounded-full border cursor-pointer transition-colors ${i === 3 || i === 4 || i === 5 ? 'bg-cyan-500 text-white border-cyan-500 shadow-md' : 'bg-white text-slate-600 border-slate-200 hover:border-cyan-500 hover:text-cyan-600'}`}
                >
                  {spec}
                </span>
              ))}
            </div>
          </div>
        </div>
      </section>

      {/* Ask Doctors */}
      <section id="preceptors-section" className="px-6 py-24 mx-auto max-w-7xl lg:px-8 scroll-mt-10">
        <div className="grid grid-cols-1 gap-12 lg:grid-cols-4">
          <div className="lg:col-span-1">
            <h2 className="mb-8 text-4xl font-black leading-tight text-slate-800">
              Ask<br/>Doctors
            </h2>
            <div className="flex flex-col gap-2">
              {['All', 'Primary Care', 'Family Medicine', 'Pediatric', 'Emergency', 'Orthopaedic'].map((cat) => (
                <button 
                  key={cat} 
                  onClick={() => setActiveSpecialty(cat)}
                  className={`px-6 py-3 text-sm font-bold text-left rounded-r-full transition-all ${activeSpecialty === cat ? 'bg-cyan-200/50 text-cyan-800 border-l-4 border-cyan-500' : 'text-slate-600 hover:bg-slate-50 border-l-4 border-transparent'}`}
                >
                  {cat}
                </button>
              ))}
            </div>
          </div>
          
          <div className="relative lg:col-span-3">
            <div className="grid grid-cols-1 gap-6 md:grid-cols-2">
              {isLoading ? (
                Array.from({ length: 6 }).map((_, i) => (
                  <div key={i} className="h-32 animate-pulse bg-slate-200 rounded-2xl" />
                ))
              ) : preceptors.length > 0 ? (
                preceptors.map((doc) => (
                  <div key={doc.userId} className="flex items-center gap-4 p-4 transition-all bg-white border border-transparent shadow-sm rounded-2xl ring-1 ring-slate-100 hover:shadow-md hover:border-cyan-100">
                    <div className="w-16 h-16 rounded-xl bg-cyan-50 flex items-center justify-center text-cyan-500 overflow-hidden">
                      {/* Using a placeholder because real preceptor profiles don't store image URLs yet */}
                      <span className="material-symbols-outlined text-3xl">person</span>
                    </div>
                    <div className="flex-1">
                      <h4 className="font-bold text-slate-800">{doc.displayName}</h4>
                      <p className="text-[11px] text-slate-500 font-medium">{doc.specialty || 'General Practice'} • {doc.credentials}</p>
                      <div className="flex items-center gap-4 mt-2">
                        <div className="flex items-center gap-1 text-xs font-semibold text-slate-400">
                          <span className="text-[14px] material-symbols-outlined text-amber-400" style={{ fontVariationSettings: "'FILL' 1" }}>thumb_up</span>
                          {doc.isVerified ? 'Verified' : 'Reviewing'}
                        </div>
                        <div className="flex items-center gap-1 text-xs font-semibold text-slate-400">
                          <span className="text-[14px] material-symbols-outlined">location_on</span>
                          {doc.location || 'N/A'}
                        </div>
                      </div>
                    </div>
                    <button 
                      onClick={() => navigate('/login')}
                      className="px-5 py-2 text-xs font-bold text-white transition-colors rounded-full shadow-sm bg-[#39b54a] hover:bg-[#2d9e3d]"
                    >
                      CHAT
                    </button>
                  </div>
                ))
              ) : (
                <div className="col-span-2 py-20 text-center text-slate-400 font-medium italic border-2 border-dashed border-slate-200 rounded-3xl">
                  No preceptors found matching this specialty.
                </div>
              )}
            </div>
            
            <div className="flex justify-end mt-8">
               <button 
                onClick={() => window.scrollTo({ top: 0, behavior: 'smooth' })}
                className="flex items-center justify-center w-10 h-10 text-white transition-colors rounded-full bg-cyan-300 hover:bg-cyan-400 shadow-cyan-500/20 shadow-lg"
               >
                 <span className="material-symbols-outlined">arrow_upward</span>
               </button>
            </div>
          </div>
        </div>
      </section>

      {/* Testimonials */}
      <section className="relative px-6 py-20 mx-auto max-w-7xl lg:px-8">
         <div className="absolute right-0 top-0 w-[400px] h-[400px] bg-cyan-50 rounded-tl-full rounded-bl-full -z-10" />
         <div className="absolute left-[-5%] bottom-0 w-[300px] h-[300px] bg-slate-50 rounded-tr-full rounded-br-full -z-10" />
         
         <div className="grid grid-cols-1 gap-12 lg:grid-cols-3">
           <div className="flex items-center justify-center lg:order-last">
             <div className="text-center lg:text-left">
                <span className="text-6xl text-cyan-300 material-symbols-outlined" style={{ fontVariationSettings: "'FILL' 0" }}>format_quote</span>
                <h2 className="mt-2 text-4xl font-black text-cyan-500">What People<br/>Are Saying</h2>
             </div>
           </div>
           
           <div className="grid grid-cols-1 gap-8 md:grid-cols-2 lg:col-span-2">
              {[
                { name: 'Sarah Johnson', role: 'Nursing Student', text: 'NPaxis made finding a preceptor so easy! I was struggling to find a placement for my pediatric rotation, but through this platform, I connected with an amazing mentor who helped me excel in my clinicals.', img: 'https://images.unsplash.com/photo-1599566150163-29194dcaad36?w=100' },
                { name: 'Dr. Michael Chen', role: 'Certified Preceptor', text: 'As a preceptor, I love how easy it is to manage my availability and connect with dedicated students. The platform handles all the organization, so I can focus on teaching and mentorship.', img: 'https://images.unsplash.com/photo-1438761681033-6461ffad8d80?w=100' }
              ].map((test, i) => (
                 <div key={i} className={`relative p-8 bg-white border border-slate-100 shadow-xl rounded-bl-[60px] rounded-tr-[40px] rounded-tl-xl rounded-br-xl ${i===1?'lg:mt-12':''}`}>
                    <p className="text-sm leading-relaxed text-slate-600 italic">
                      "{test.text}"
                    </p>
                    <div className="flex gap-1 mt-4 text-amber-400">
                      <span className="text-[16px] material-symbols-outlined" style={{ fontVariationSettings: "'FILL' 1" }}>star</span>
                      <span className="text-[16px] material-symbols-outlined" style={{ fontVariationSettings: "'FILL' 1" }}>star</span>
                      <span className="text-[16px] material-symbols-outlined" style={{ fontVariationSettings: "'FILL' 1" }}>star</span>
                      <span className="text-[16px] material-symbols-outlined" style={{ fontVariationSettings: "'FILL' 1" }}>star</span>
                      <span className="text-[16px] material-symbols-outlined" style={{ fontVariationSettings: "'FILL' 1" }}>star</span>
                    </div>
                    <div className="absolute right-6 -bottom-6 flex items-center gap-3">
                       <div className="text-right">
                         <p className="text-sm font-bold text-slate-800">{test.name}</p>
                         <p className="text-[10px] uppercase font-bold text-slate-400 tracking-wider">{test.role}</p>
                       </div>
                       <img src={test.img} className="w-14 h-14 object-cover rounded-full border-4 border-white shadow-md" alt={test.name} />
                    </div>
                 </div>
              ))}
           </div>
         </div>
      </section>

      {/* CTA Box */}
      <section className="px-6 py-12 mx-auto max-w-7xl lg:px-8">
        <div className="flex flex-col items-center justify-between p-8 bg-white border shadow-sm md:flex-row border-cyan-300 rounded-bl-[40px] rounded-tr-[40px] rounded-tl-xl rounded-br-xl">
          <div className="mb-6 md:mb-0">
            <h3 className="text-2xl font-bold text-slate-800">Ready to accelerate your career?</h3>
            <p className="mt-1 text-sm text-slate-500">Join hundreds of students and preceptors who are already transforming clinical education.</p>
          </div>
          <button 
            onClick={() => navigate('/register')}
            className="px-8 py-3 text-sm font-bold tracking-wide text-white uppercase transition-colors rounded-full shadow-md bg-[#39b54a] hover:bg-[#2d9e3d]"
          >
            Get Started
          </button>
        </div>
      </section>

      {/* Footer */}
      <footer id="contact-section" className="pt-16 pb-8 border-t border-slate-100 scroll-mt-10">
        <div className="grid grid-cols-1 gap-8 px-6 mx-auto max-w-7xl md:grid-cols-4 lg:grid-cols-5 lg:px-8">
          <div className="lg:col-span-2">
             <div className="flex items-center gap-2 mb-4">
                <span className="text-3xl text-slate-300 material-symbols-outlined">health_and_safety</span>
                <span className="text-xl font-black text-slate-800">NPaxis</span>
             </div>
             <p className="text-xs leading-relaxed text-slate-400 max-w-xs">
               NPaxis is a leading platform connecting nursing and medical students with verified preceptors for quality clinical rotations globally.
             </p>
          </div>
          
          <div>
            <h4 className="mb-4 text-xs font-bold tracking-wider uppercase text-cyan-500">Company</h4>
            <ul className="space-y-3 text-xs font-semibold text-slate-500">
              <li><a href="#" className="hover:text-cyan-500">About NPaxis</a></li>
              <li><a href="#" className="hover:text-cyan-500">Preceptors</a></li>
              <li><a href="#" className="hover:text-cyan-500">Students</a></li>
              <li><a href="#" className="hover:text-cyan-500">Contact Us</a></li>
            </ul>
          </div>

          <div>
            <h4 className="mb-4 text-xs font-bold tracking-wider uppercase text-cyan-500">Services</h4>
            <ul className="space-y-3 text-xs font-semibold text-slate-500">
              <li><a href="#" className="hover:text-cyan-500">Find Preceptors</a></li>
              <li><a href="#" className="hover:text-cyan-500">Verify Licenses</a></li>
              <li><a href="#" className="hover:text-cyan-500">Premium Memberships</a></li>
              <li><a href="#" className="hover:text-cyan-500">Support Center</a></li>
            </ul>
          </div>

          <div>
             <div className="flex gap-3 mb-6">
                <a href="#" className="flex items-center justify-center w-8 h-8 rounded-full bg-cyan-50 text-cyan-500 hover:bg-cyan-500 hover:text-white transition-colors">f</a>
                <a href="#" className="flex items-center justify-center w-8 h-8 rounded-full bg-cyan-50 text-cyan-500 hover:bg-cyan-500 hover:text-white transition-colors">in</a>
                <a href="#" className="flex items-center justify-center w-8 h-8 rounded-full bg-cyan-50 text-cyan-500 hover:bg-cyan-500 hover:text-white transition-colors">t</a>
                <a href="#" className="flex items-center justify-center w-8 h-8 rounded-full bg-cyan-50 text-cyan-500 hover:bg-cyan-500 hover:text-white transition-colors">m</a>
             </div>
             
             <div className="inline-block relative">
               <select className="appearance-none border border-slate-200 rounded-md py-1.5 pl-3 pr-8 text-xs font-semibold text-cyan-600 bg-white shadow-sm outline-none focus:border-cyan-500">
                 <option>English - En</option>
                 <option>Spanish - Es</option>
               </select>
               <span className="absolute right-2 top-1/2 -translate-y-1/2 text-cyan-500 material-symbols-outlined text-[16px] pointer-events-none">expand_more</span>
             </div>
          </div>
        </div>
        <div className="px-6 mx-auto mt-12 max-w-7xl lg:px-8 text-center text-[11px] font-semibold text-slate-400">
           &copy; {new Date().getFullYear()} Digitalearn Solution Pvt. Ltd. All rights reserved.
        </div>
      </footer>
    </div>
  );
};

export default Landing;
