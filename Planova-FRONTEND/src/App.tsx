/**
 * App.tsx - Main application component with routing
 * Planova Frontend - Glassmorphism UI
 */

import { Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import ProtectedRoute from './components/common/ProtectedRoute';
import Layout from './components/layout/Layout';

// Pages
import LoginPage from './pages/Login';
import RegisterPage from './pages/Register';
import HomePage from './pages/Home';
import KanbanPage from './pages/Kanban';
import PerfilPage from './pages/Perfil';

function App() {
  return (
    <AuthProvider>
      <Routes>
        {/* Public Routes */}
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
        
        {/* Protected Routes wrapped in Layout */}
        <Route 
          path="/home" 
          element={
            <ProtectedRoute>
              <Layout>
                <HomePage />
              </Layout>
            </ProtectedRoute>
          } 
        />
        <Route 
          path="/proyecto/:id" 
          element={
            <ProtectedRoute>
              <Layout>
                <KanbanPage />
              </Layout>
            </ProtectedRoute>
          } 
        />
        <Route 
          path="/perfil" 
          element={
            <ProtectedRoute>
              <Layout>
                <PerfilPage />
              </Layout>
            </ProtectedRoute>
          } 
        />
        
        {/* Default redirect */}
        <Route path="/" element={<Navigate to="/login" replace />} />
        <Route path="*" element={<Navigate to="/login" replace />} />
      </Routes>
    </AuthProvider>
  );
}

export default App;
