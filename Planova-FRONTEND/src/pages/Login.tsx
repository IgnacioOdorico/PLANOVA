/**
 * Login Page for Planova
 * Glassmorphism design replicating docs/assets/login.png
 */

import { useState, FormEvent } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import mascotImg from '../../docs/assets/img para utilizar/Personaje de planova en azul.png';
import { useAuth } from '../context/AuthContext';
import { getApiErrorMessage } from '../services/api';

export const LoginPage: React.FC = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [isLoading, setIsLoading] = useState(false);

  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  // Get the redirect path from location state or default to home
  const from = (location.state as { from?: { pathname: string } })?.from?.pathname || '/home';

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setError('');
    setIsLoading(true);

    try {
      await login({ email, password });
      navigate(from, { replace: true });
    } catch (err: unknown) {
      const message = getApiErrorMessage(err);
      setError(message);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center p-4 relative overflow-hidden">
      {/* Animated background elements */}
      <div className="absolute inset-0 overflow-hidden pointer-events-none">
        {/* Floating orbs */}
        <div 
          className="absolute w-96 h-96 rounded-full opacity-30 animate-float"
          style={{
            background: 'radial-gradient(circle, rgba(99, 102, 241, 0.4) 0%, transparent 70%)',
            top: '10%',
            left: '10%',
          }}
        />
        <div 
          className="absolute w-80 h-80 rounded-full opacity-25 animate-float-delayed"
          style={{
            background: 'radial-gradient(circle, rgba(139, 92, 246, 0.4) 0%, transparent 70%)',
            bottom: '15%',
            right: '10%',
          }}
        />
        <div 
          className="absolute w-64 h-64 rounded-full opacity-20 animate-float"
          style={{
            background: 'radial-gradient(circle, rgba(59, 130, 246, 0.4) 0%, transparent 70%)',
            top: '60%',
            left: '60%',
          }}
        />
      </div>

      {/* Main glass card */}
      <div className="relative mt-20 z-10 w-full max-w-lg">
        {/* Mascot protruding */}
        <div className="absolute -top-[125px] -left-10 z-20 pointer-events-none">
          <img 
            src={mascotImg}
            alt="Planova Mascot" 
            className="w-48 object-contain"
            style={{ filter: 'drop-shadow(0px 10px 15px rgba(0,0,0,0.6))' }}
          />
        </div>

        <div className="glass-extreme w-full px-12 pt-14 pb-10 relative z-0">
          
          <div className="text-center mb-6">
            <h1 className="text-3xl font-medium tracking-[0.2em] text-white">PLANOVA</h1>
            <div className="flex items-center justify-center gap-2 mt-1 mb-2 opacity-50">
              <span className="w-12 h-px bg-white"></span>
              <span className="text-[10px] tracking-widest font-serif">PLANOVA</span>
              <span className="w-12 h-px bg-white"></span>
            </div>
          </div>

          {/* Welcome text */}
          <div className="mb-8 mt-2">
            <p className="text-white/90 text-xl leading-relaxed" style={{ fontFamily: 'Kalam, cursive' }}>
              bienvenidos a Planova, planova es un<br/>
              gestor de tareas, para q puedas estar<br/>
              siempre organizado
            </p>
          </div>

          {/* Login Form */}
          <form onSubmit={handleSubmit} className="space-y-6">
            {/* Error Message */}
            {error && (
              <div 
                className="p-3 rounded-xl text-sm text-red-200 flex flex-col items-center gap-1 text-center"
                style={{
                  background: 'rgba(239, 68, 68, 0.15)',
                  border: '1px solid rgba(239, 68, 68, 0.3)',
                }}
              >
                {error}
              </div>
            )}

            {/* Email Input */}
            <div className="flex flex-col text-center mt-3">
              <label className="text-[1.35rem] leading-none text-white/90 mb-2" style={{ fontFamily: 'Kalam, cursive' }}>ingrese su email</label>
              <input
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                required
                className="glass-input-extreme w-full py-1.5 px-4 text-center text-lg focus:ring-1 focus:ring-white/20"
              />
            </div>

            {/* Password Input */}
            <div className="flex flex-col text-center mt-3 mb-4">
              <label className="text-[1.35rem] leading-none text-white/90 mb-2" style={{ fontFamily: 'Kalam, cursive' }}>contraseña</label>
              <input
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
                className="glass-input-extreme w-full py-1.5 px-4 text-center text-lg focus:ring-1 focus:ring-white/20"
              />
            </div>

            {/* Submit Button */}
            <div className="pt-8 mb-2 flex justify-center">
              <button
                type="submit"
                disabled={isLoading}
                className="glass-btn-extreme px-12 py-1.5 text-[1.4rem] text-white hover:text-white/90 active:scale-[0.98] disabled:opacity-50"
                style={{ fontFamily: 'Kalam, cursive' }}
              >
                {isLoading ? '...' : 'iniciar sesión'}
              </button>
            </div>
          </form>

          {/* Register Link */}
          <div className="mt-8 text-center pb-2">
            <p className="text-white/70 text-[1.1rem]" style={{ fontFamily: 'Kalam, cursive' }}>
              ¿no tienes una cuenta?{' '}
              <Link 
                to="/register" 
                className="text-white hover:text-indigo-200 transition-colors duration-200"
              >
                regístrate
              </Link>
            </p>
          </div>
        </div>
      </div>
    </div>
  );
};

export default LoginPage;
