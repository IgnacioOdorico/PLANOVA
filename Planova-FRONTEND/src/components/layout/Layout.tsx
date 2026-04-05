/**
 * Layout Component for Planova
 * Glassmorphism header with navigation, wraps protected pages
 */

import { ReactNode, useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import GlassButton from '../common/GlassButton';

interface LayoutProps {
  children: ReactNode;
}

const Layout: React.FC<LayoutProps> = ({ children }) => {
  const { user, logout } = useAuth();
  const location = useLocation();
  const navigate = useNavigate();
  const [isLoggingOut, setIsLoggingOut] = useState(false);

  const handleLogout = async () => {
    setIsLoggingOut(true);
    try {
      await logout();
      navigate('/login', { replace: true });
    } catch (error) {
      console.error('Logout error:', error);
      // Force redirect anyway
      navigate('/login', { replace: true });
    } finally {
      setIsLoggingOut(false);
    }
  };

  const navLinks = [
    { path: '/home', label: 'Home', icon: (
      <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6" />
      </svg>
    )},
    { path: '/perfil', label: 'Perfil', icon: (
      <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
      </svg>
    )},
  ];

  return (
    <div className="min-h-screen flex flex-col">
      {/* Glass Header */}
      <header className="glass-header sticky top-0 z-50 px-6 py-4">
        <div className="max-w-7xl mx-auto flex items-center justify-between">
          {/* Logo */}
          <Link 
            to="/home" 
            className="flex items-center gap-3 group"
          >
            {/* Planova Logo */}
            <div 
              className="w-10 h-10 rounded-xl flex items-center justify-center transition-transform duration-300 group-hover:scale-105"
              style={{
                background: 'linear-gradient(135deg, rgba(99, 102, 241, 0.4), rgba(139, 92, 246, 0.4))',
                border: '1px solid rgba(255, 255, 255, 0.2)',
                boxShadow: '0 0 20px rgba(99, 102, 241, 0.3)',
              }}
            >
              <svg 
                viewBox="0 0 24 24" 
                className="w-6 h-6"
                fill="none"
                xmlns="http://www.w3.org/2000/svg"
              >
                <defs>
                  <linearGradient id="headerLogoGradient" x1="0%" y1="0%" x2="100%" y2="100%">
                    <stop offset="0%" stopColor="#818cf8" />
                    <stop offset="100%" stopColor="#c084fc" />
                  </linearGradient>
                </defs>
                <path 
                  d="M5 21V3h4v8c2 0 3.5 1.5 4 4 0.5-2.5 2-4 4-4V3h4v18H5z"
                  fill="url(#headerLogoGradient)"
                />
              </svg>
            </div>
            <span 
              className="text-xl font-bold hidden sm:block"
              style={{
                background: 'linear-gradient(135deg, #e0e7ff 0%, #c7d2fe 100%)',
                WebkitBackgroundClip: 'text',
                WebkitTextFillColor: 'transparent',
              }}
            >
              Planova
            </span>
          </Link>

          {/* Navigation Links */}
          <nav className="flex items-center gap-2">
            {navLinks.map((link) => {
              const isActive = location.pathname === link.path;
              return (
                <Link
                  key={link.path}
                  to={link.path}
                  className={`
                    flex items-center gap-2 px-4 py-2 rounded-lg transition-all duration-300
                    ${isActive 
                      ? 'bg-white/10 text-white' 
                      : 'text-white/60 hover:text-white hover:bg-white/5'
                    }
                  `}
                >
                  {link.icon}
                  <span className="font-medium">{link.label}</span>
                  {isActive && (
                    <div 
                      className="absolute bottom-0 left-0 right-0 h-0.5 rounded-full"
                      style={{
                        background: 'linear-gradient(90deg, #818cf8, #c084fc)',
                      }}
                    />
                  )}
                </Link>
              );
            })}
          </nav>

          {/* User & Logout */}
          <div className="flex items-center gap-4">
            {/* User Info */}
            <div className="hidden md:flex items-center gap-3">
              <div 
                className="w-8 h-8 rounded-full flex items-center justify-center text-sm font-medium"
                style={{
                  background: 'linear-gradient(135deg, rgba(99, 102, 241, 0.3), rgba(139, 92, 246, 0.3))',
                  border: '1px solid rgba(255, 255, 255, 0.15)',
                }}
              >
                {user?.nombre?.charAt(0).toUpperCase() || 'U'}
              </div>
              <span className="text-white/80 text-sm">
                {user?.nombre || 'Usuario'}
              </span>
            </div>

            {/* Logout Button */}
            <GlassButton
              variant="secondary"
              size="sm"
              onClick={handleLogout}
              isLoading={isLoggingOut}
              leftIcon={
                <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path 
                    strokeLinecap="round" 
                    strokeLinejoin="round" 
                    strokeWidth={2} 
                    d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1" 
                  />
                </svg>
              }
            >
              <span className="hidden sm:inline">Salir</span>
            </GlassButton>
          </div>
        </div>
      </header>

      {/* Main Content */}
      <main className="flex-1">
        {children}
      </main>
    </div>
  );
};

export default Layout;