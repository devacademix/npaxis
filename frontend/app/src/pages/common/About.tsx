import React, { useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';

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
    const resize = () => { w = canvas.width = parent.clientWidth; h = canvas.height = parent.clientHeight; };
    window.addEventListener('resize', resize);
    class Particle {
      x: number; y: number; z: number; vx: number; vy: number; size: number; color: string;
      constructor() {
        this.x = Math.random() * w; this.y = Math.random() * h; this.z = Math.random() * 2;
        this.vx = (Math.random() - 0.5) * 0.3; this.vy = (Math.random() - 0.5) * 0.3;
        this.size = Math.random() * 2 + 1;
        const colors = ['rgba(6,182,212,0.4)', 'rgba(56,189,248,0.3)', 'rgba(16,185,129,0.2)'];
        this.color = colors[Math.floor(Math.random() * colors.length)];
      }
      update() {
        this.x += this.vx; this.y += this.vy;
        if (this.x < 0 || this.x > w) this.vx *= -1;
        if (this.y < 0 || this.y > h) this.vy *= -1;
      }
      draw() {
        if (!ctx) return;
        ctx.beginPath(); ctx.arc(this.x, this.y, this.size * this.z, 0, Math.PI * 2);
        ctx.fillStyle = this.color; ctx.fill();
      }
    }
    const particles = Array.from({ length: 50 }, () => new Particle());
    let animationFrameId: number;
    const animate = () => {
      ctx.clearRect(0, 0, w, h);
      particles.forEach(p => { p.update(); p.draw(); });
      animationFrameId = requestAnimationFrame(animate);
    };
    animate();
    return () => { window.removeEventListener('resize', resize); cancelAnimationFrame(animationFrameId); };
  }, []);
  return <canvas ref={canvasRef} className={className || "absolute top-0 left-0 w-full h-full -z-10 pointer-events-none"} />;
};

const About: React.FC = () => {
  const navigate = useNavigate();

  return (
    <div className="min-h-screen bg-white font-sans text-slate-800 overflow-x-hidden">
      <ParticleBackground />
      
      {/* Navbar */}
      <nav className="relative z-50 flex items-center justify-between px-6 py-6 mx-auto max-w-7xl lg:px-8">
        <div className="flex items-center gap-3 cursor-pointer" onClick={() => navigate('/')}>
          <div className="flex items-center justify-center w-10 h-10 text-white rounded-full bg-cyan-500 shadow-lg">
            <span className="text-xl font-bold material-symbols-outlined">health_and_safety</span>
          </div>
          <span className="text-2xl font-black tracking-tight text-slate-900">NPaxis</span>
        </div>
        <div className="hidden md:flex items-center gap-8">
          <button onClick={() => navigate('/')} className="text-sm font-bold text-slate-600 hover:text-cyan-500">Home</button>
          <button onClick={() => navigate('/browse')} className="text-sm font-bold text-slate-600 hover:text-cyan-500">Browse</button>
          <button onClick={() => navigate('/contact')} className="px-6 py-2 text-sm font-bold text-white bg-cyan-500 rounded-full">Contact Us</button>
        </div>
      </nav>

      {/* Hero Section */}
      <section className="relative px-6 py-20 mx-auto max-w-7xl lg:px-8 text-center">
        <div className="inline-block px-4 py-1 mb-6 text-xs font-black tracking-widest text-cyan-600 uppercase bg-cyan-50 rounded-full">Our Story</div>
        <h1 className="text-5xl lg:text-7xl font-black tracking-tight text-slate-900 leading-tight">
          Bridging the gap between <br />
          <span className="text-cyan-500">Learning & Practice</span>
        </h1>
        <p className="mt-8 text-xl text-slate-500 max-w-3xl mx-auto leading-relaxed">
          NPaxis is a state-of-the-art clinical rotation management platform dedicated to connecting medical and nursing students with the best preceptors globally.
        </p>
      </section>

      {/* Mission & Vision */}
      <section className="py-24 bg-slate-50">
        <div className="px-6 mx-auto max-w-7xl lg:px-8">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-12">
            <div className="p-10 bg-white rounded-[40px] shadow-sm border border-slate-100 hover:shadow-xl transition-all">
              <div className="w-14 h-14 bg-emerald-100 text-emerald-600 rounded-2xl flex items-center justify-center mb-6">
                 <span className="material-symbols-outlined text-3xl">rocket_launch</span>
              </div>
              <h3 className="text-3xl font-black text-slate-900 mb-4">Our Mission</h3>
              <p className="text-slate-500 leading-relaxed font-medium">
                To simplify the clinical placement process, ensuring every student has access to high-quality mentorship and real-world clinical experience that accelerates their professional growth.
              </p>
            </div>
            <div className="p-10 bg-white rounded-[40px] shadow-sm border border-slate-100 hover:shadow-xl transition-all">
              <div className="w-14 h-14 bg-cyan-100 text-cyan-600 rounded-2xl flex items-center justify-center mb-6">
                 <span className="material-symbols-outlined text-3xl">visibility</span>
              </div>
              <h3 className="text-3xl font-black text-slate-900 mb-4">Our Vision</h3>
              <p className="text-slate-500 leading-relaxed font-medium">
                To become the global standard for clinical education networking, where trust, transparency, and excellence are at the heart of every student-preceptor connection.
              </p>
            </div>
          </div>
        </div>
      </section>

      {/* Stats */}
      <section className="py-24">
        <div className="px-6 mx-auto max-w-7xl lg:px-8">
          <div className="grid grid-cols-2 lg:grid-cols-4 gap-8">
            {[
              { label: 'Verified Preceptors', value: '250+' },
              { label: 'Successful Placements', value: '1,200+' },
              { label: 'Partner Schools', value: '45+' },
              { label: 'Specialties Covered', value: '30+' },
            ].map((stat, i) => (
              <div key={i} className="text-center group">
                <p className="text-5xl font-black text-cyan-500 transition-transform group-hover:scale-110 duration-300">{stat.value}</p>
                <p className="mt-2 text-sm font-bold text-slate-400 uppercase tracking-widest">{stat.label}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* Values */}
      <section className="py-24 bg-cyan-500 overflow-hidden relative">
        <div className="absolute top-0 right-0 w-96 h-96 bg-white/10 rounded-full -translate-y-1/2 translate-x-1/2 blur-3xl" />
        <div className="px-6 mx-auto max-w-7xl lg:px-8 text-center relative z-10">
          <h2 className="text-4xl font-black text-white mb-16">Why Choose NPaxis?</h2>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-12">
            {[
              { icon: 'verified_user', title: 'Trusted Network', desc: 'Every preceptor goes through a rigorous license verification process.' },
              { icon: 'bolt', title: 'Seamless Matching', desc: 'Our smart filters help you find the perfect match in seconds.' },
              { icon: 'headset_mic', title: 'Dedicated Support', desc: 'Our team is here to assist you at every step of your clinical journey.' },
            ].map((val, i) => (
              <div key={i} className="text-white">
                <div className="w-16 h-16 bg-white/20 rounded-full flex items-center justify-center mx-auto mb-6">
                  <span className="material-symbols-outlined text-white text-3xl">{val.icon}</span>
                </div>
                <h4 className="text-xl font-bold mb-3">{val.title}</h4>
                <p className="text-cyan-50 text-sm leading-relaxed opacity-80">{val.desc}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* Footer */}
      <footer className="py-16 text-center border-t border-slate-100">
         <p className="text-sm font-bold text-slate-400 uppercase tracking-widest">&copy; {new Date().getFullYear()} NPaxis Clinical Platform</p>
         <div className="mt-4 flex justify-center gap-6">
            <button onClick={() => navigate('/')} className="text-xs font-bold text-cyan-500">Back to Home</button>
            <button onClick={() => navigate('/contact')} className="text-xs font-bold text-cyan-500">Support</button>
         </div>
      </footer>
    </div>
  );
};

export default About;
