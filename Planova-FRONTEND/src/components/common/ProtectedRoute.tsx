/**
 * ProtectedRoute Component for Planova
 * Route guard that checks authentication state
 */

import { ReactNode } from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';

interface ProtectedRouteProps {
  children: ReactNode;
}

export const ProtectedRoute: React.FC<ProtectedRouteProps> = ({ children }) => {
  const { isAuthenticated, isLoading } = useAuth();
  const location = useLocation();

  if (isLoading) {
    // Show loading spinner while checking auth state
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="glass-card p-8 rounded-glass-lg animate-pulse-glow">
          <div className="flex items-center gap-4">
            <div className="w-8 h-8 border-2 border-white/30 border-t-white/80 rounded-full animate-spin" />
            <span className="text-white/80 text-lg">Cargando...</span>
          </div>
        </div>
      </div>
    );
  }

  if (!isAuthenticated) {
    // Redirect to login page with return URL
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  return <>{children}</>;
};

export default ProtectedRoute;
