/**
 * Auth Context for Planova
 * JWT authentication state management with localStorage persistence
 */

import { createContext, useContext, useState, useEffect, ReactNode, useCallback } from 'react';
import { Usuario, LoginRequest, RegisterRequest } from '../types';
import { 
  authApi, 
  getToken, 
  setToken, 
  removeToken, 
  getUser, 
  setUser, 
  removeUser,
  getApiErrorMessage 
} from '../services/api';

interface AuthContextType {
  user: Usuario | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  error: string | null;
  login: (credentials: LoginRequest) => Promise<void>;
  register: (userData: RegisterRequest) => Promise<void>;
  logout: () => Promise<void>;
  refreshUser: () => void;
  clearError: () => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

interface AuthProviderProps {
  children: ReactNode;
}

export const AuthProvider: React.FC<AuthProviderProps> = ({ children }) => {
  const [user, setUserState] = useState<Usuario | null>(null);
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  // Initialize auth state from localStorage on mount
  useEffect(() => {
    const initAuth = () => {
      const token = getToken();
      const storedUser = getUser();
      
      if (token && storedUser) {
        setUserState(storedUser);
      } else if (token) {
        // Token exists but no user - clean up
        removeToken();
        removeUser();
      }
      setIsLoading(false);
    };

    initAuth();
  }, []);

  const login = useCallback(async (credentials: LoginRequest): Promise<void> => {
    setIsLoading(true);
    setError(null);
    
    try {
      const response = await authApi.login(credentials);
      const { token, usuario } = response;
      
      setToken(token);
      
      // Use user from backend response — no extra roundtrip
      const loggedInUser: Usuario = {
        id: usuario.id,
        nombre: usuario.nombre,
        email: usuario.email,
      };
      setUser(loggedInUser);
      setUserState(loggedInUser);
    } catch (err) {
      const message = getApiErrorMessage(err);
      setError(message);
      throw err;
    } finally {
      setIsLoading(false);
    }
  }, []);

  const register = useCallback(async (userData: RegisterRequest): Promise<void> => {
    setIsLoading(true);
    setError(null);
    
    try {
      const response = await authApi.register(userData);
      const { token, usuario } = response;
      
      setToken(token);
      
      // Use user from backend response — real ID, no extra roundtrip
      const newUser: Usuario = {
        id: usuario.id,
        nombre: usuario.nombre,
        email: usuario.email,
      };
      setUser(newUser);
      setUserState(newUser);
    } catch (err) {
      const message = getApiErrorMessage(err);
      setError(message);
      throw err;
    } finally {
      setIsLoading(false);
    }
  }, []);

  const logout = useCallback(async (): Promise<void> => {
    setIsLoading(true);
    
    try {
      await authApi.logout();
    } catch {
      // Ignore logout errors - clean up anyway
    } finally {
      removeToken();
      removeUser();
      setUserState(null);
      setIsLoading(false);
    }
  }, []);

  // Refresh user state from localStorage (useful after profile update)
  const refreshUser = useCallback(() => {
    const storedUser = getUser();
    if (storedUser) {
      setUserState(storedUser);
    }
  }, []);

  const clearError = useCallback(() => {
    setError(null);
  }, []);

  const value: AuthContextType = {
    user,
    isAuthenticated: !!user,
    isLoading,
    error,
    login,
    register,
    logout,
    refreshUser,
    clearError,
  };

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = (): AuthContextType => {
  const context = useContext(AuthContext);
  
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  
  return context;
};

// Export the context for access to auth utilities if needed
export { AuthContext };
