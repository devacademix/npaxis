import React, { useEffect, useRef, useState } from 'react';
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
    
    const resize = () => {
      w = canvas.width = parent.clientWidth;
      h = canvas.height = parent.clientHeight;
    };
    window.addEventListener('resize', resize);

    class Particle {
      x: number; y: number; z: number; vx: number; vy: number; size: number; color: string;
      constructor() {
        this.x = Math.random() * w;
        this.y = Math.random() * h;
        this.z = Math.random() * 2;
        this.vx = (Math.random() - 0.5) * 0.5;
        this.vy = (Math.random() - 0.5) * 0.5;
        this.size = Math.random() * 3 + 1;
        const colors = ['rgba(6,182,212,0.8)', 'rgba(56,189,248,0.6)', 'rgba(16,185,129,0.5)'];
        this.color = colors[Math.floor(Math.random() * colors.length)];
      }
      update(mouseX: number, mouseY: number) {
        this.x += this.vx * (1 + this.z);
        this.y += this.vy * (1 + this.z);
        if (this.x < 0 || this.x > w) this.vx *= -1;
        if (this.y < 0 || this.y > h) this.vy *= -1;
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

    const particles: Particle[] = Array.from({ length: 40 }, () => new Particle());
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

  return <canvas ref={canvasRef} className={className || "absolute top-0 left-0 w-full h-full -z-10 pointer-events-none"} />;
};

const Contact: React.FC = () => {
  const navigate = useNavigate();
  const [formState, setFormState] = useState({ name: '', email: '', subject: '', message: '' });
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [submitted, setSubmitted] = useState(false);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    setIsSubmitting(true);
    setTimeout(() => {
      setIsSubmitting(false);
      setSubmitted(true);
    }, 1500);
  };

  return (
    <div className="relative min-h-screen bg-white font-sans text-slate-800 overflow-x-hidden pt-8">
      <ParticleBackground />
      <div className="absolute top-[-10%] left-[-10%] w-[60%] h-[1000px] bg-cyan-50/40 rounded-full blur-3xl -z-20 pointer-events-none" />
      
      {/* Header */}
      <nav className="relative z-50 flex items-center justify-between px-6 py-4 mx-auto max-w-7xl lg:px-8">
         <div className="flex items-center gap-3 cursor-pointer" onClick={() => navigate('/')}>
          <div className="flex items-center justify-center w-10 h-10 text-white rounded-full bg-cyan-500 shadow-md">
            <span className="text-xl font-bold material-symbols-outlined">health_and_safety</span>
          </div>
          <span className="text-2xl font-black tracking-tight text-slate-900">NPaxis</span>
        </div>
        <button onClick={() => navigate('/')} className="px-6 py-2 text-sm font-bold text-cyan-600 hover:text-cyan-700 transition-colors">
          Back to Home
        </button>
      </nav>

      {/* Main Content */}
      <div className="relative z-10 px-6 py-12 mx-auto max-w-7xl lg:px-8 lg:py-20">
        <div className="grid grid-cols-1 gap-16 lg:grid-cols-2">
          
          {/* Left: Contact Info */}
          <div>
            <h1 className="text-5xl font-black tracking-tight text-slate-900 leading-tight">
              Get in touch <br/> with our experts
            </h1>
            <p className="mt-6 text-lg text-slate-500 max-w-sm">
              Have questions about preceptors, clinical rotations, or your membership? We're here to help you succeed.
            </p>

            <div className="mt-12 space-y-8">
              {[
                { icon: 'mail', title: 'Email Us', desc: 'support@npaxis.com', color: 'bg-cyan-100 text-cyan-600' },
                { icon: 'call', title: 'Phone Support', desc: '+1 (555) 123-4567', color: 'bg-emerald-100 text-emerald-600' },
                { icon: 'location_on', title: 'Our Office', desc: '123 Medical Plaza, San Francisco, CA', color: 'bg-amber-100 text-amber-600' },
              ].map((item, i) => (
                <div key={i} className="flex items-start gap-4 p-4 transition-all bg-white rounded-2xl border border-slate-50 shadow-sm hover:shadow-md">
                  <div className={`w-12 h-12 flex items-center justify-center rounded-xl ${item.color}`}>
                    <span className="material-symbols-outlined">{item.icon}</span>
                  </div>
                  <div>
                    <h4 className="font-bold text-slate-800">{item.title}</h4>
                    <p className="text-sm text-slate-500">{item.desc}</p>
                  </div>
                </div>
              ))}
            </div>

            <div className="mt-12 pt-12 border-t border-slate-100">
               <div className="flex gap-4">
                  {['f', 'in', 't', 'm'].map(s => (
                    <div key={s} className="w-10 h-10 rounded-full bg-slate-100 flex items-center justify-center text-slate-400 hover:bg-cyan-500 hover:text-white transition-all cursor-pointer font-bold">{s}</div>
                  ))}
               </div>
            </div>
          </div>

          {/* Right: Contact Form */}
          <div className="relative">
            <div className="absolute inset-0 bg-cyan-500/5 blur-[100px] -z-10 rounded-full" />
            
            {submitted ? (
              <div className="bg-white p-12 rounded-3xl shadow-2xl border border-slate-100 text-center space-y-6">
                <div className="w-20 h-20 bg-emerald-100 text-emerald-600 rounded-full flex items-center justify-center mx-auto text-4xl">
                  <span className="material-symbols-outlined text-[48px]">check_circle</span>
                </div>
                <h2 className="text-3xl font-black text-slate-800">Message Sent!</h2>
                <p className="text-slate-500">Thank you for reaching out. Our team will get back to you within 24 hours.</p>
                <button 
                  onClick={() => setSubmitted(false)}
                  className="px-8 py-3 bg-cyan-500 text-white font-bold rounded-full shadow-lg shadow-cyan-500/30 hover:bg-cyan-600 transition-all"
                >
                  Send another message
                </button>
              </div>
            ) : (
              <form onSubmit={handleSubmit} className="bg-white p-8 lg:p-12 rounded-[40px] shadow-2xl border border-slate-100 space-y-6">
                 <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                   <div className="space-y-2">
                     <label className="text-xs font-bold uppercase tracking-wider text-slate-400 pl-2">Full Name</label>
                     <input 
                       required
                       type="text" 
                       placeholder="Dr. John Doe"
                       className="w-full px-5 py-4 bg-slate-50 border border-slate-100 rounded-2xl outline-none focus:ring-4 focus:ring-cyan-500/10 focus:border-cyan-500 transition-all"
                       value={formState.name}
                       onChange={e => setFormState({...formState, name: e.target.value})}
                     />
                   </div>
                   <div className="space-y-2">
                     <label className="text-xs font-bold uppercase tracking-wider text-slate-400 pl-2">Email Address</label>
                     <input 
                       required
                       type="email" 
                       placeholder="john@example.com"
                       className="w-full px-5 py-4 bg-slate-50 border border-slate-100 rounded-2xl outline-none focus:ring-4 focus:ring-cyan-500/10 focus:border-cyan-500 transition-all"
                       value={formState.email}
                       onChange={e => setFormState({...formState, email: e.target.value})}
                     />
                   </div>
                 </div>
                 
                 <div className="space-y-2">
                   <label className="text-xs font-bold uppercase tracking-wider text-slate-400 pl-2">Subject</label>
                   <input 
                     required
                     type="text" 
                     placeholder="Select inquiry type..."
                     className="w-full px-5 py-4 bg-slate-50 border border-slate-100 rounded-2xl outline-none focus:ring-4 focus:ring-cyan-500/10 focus:border-cyan-500 transition-all"
                     value={formState.subject}
                     onChange={e => setFormState({...formState, subject: e.target.value})}
                   />
                 </div>

                 <div className="space-y-2">
                   <label className="text-xs font-bold uppercase tracking-wider text-slate-400 pl-2">How can we help?</label>
                   <textarea 
                     required
                     rows={4}
                     placeholder="Tell us about your requirements..."
                     className="w-full px-5 py-4 bg-slate-50 border border-slate-100 rounded-2xl outline-none focus:ring-4 focus:ring-cyan-500/10 focus:border-cyan-500 transition-all resize-none"
                     value={formState.message}
                     onChange={e => setFormState({...formState, message: e.target.value})}
                   />
                 </div>

                 <button 
                   disabled={isSubmitting}
                   type="submit" 
                   className="w-full py-4 bg-cyan-500 text-white font-bold rounded-2xl shadow-xl shadow-cyan-500/30 hover:bg-cyan-600 transition-all flex items-center justify-center gap-2"
                 >
                   {isSubmitting ? (
                     <span className="w-5 h-5 border-2 border-white/30 border-t-white rounded-full animate-spin" />
                   ) : (
                     <>
                        Send Message
                        <span className="material-symbols-outlined text-sm">send</span>
                     </>
                   )}
                 </button>
              </form>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

export default Contact;
