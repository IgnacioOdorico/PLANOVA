/**
 * Home Page for Planova
 * Protected route showing project grid
 */

import { useState, useEffect, useCallback } from 'react';
import { Proyecto, ProyectoCreate } from '../types';
import proyectoService from '../services/proyectoService';
import ProjectCard from '../components/proyectos/ProjectCard';
import CreateProjectModal from '../components/proyectos/CreateProjectModal';
import tituloPlanovaImg from '../../docs/assets/img para utilizar/titulo de planova con personaje.png';

export const HomePage: React.FC = () => {
  const [proyectos, setProyectos] = useState<Proyecto[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [isModalOpen, setIsModalOpen] = useState(false);

  // Fetch projects on mount
  const fetchProyectos = useCallback(async () => {
    try {
      setIsLoading(true);
      setError(null);
      const data = await proyectoService.getAll();
      setProyectos(data);
    } catch (err) {
      console.error('Error fetching projects:', err);
      setError('Error al cargar los proyectos');
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchProyectos();
  }, [fetchProyectos]);

  // Handle new project creation
  const handleCreateProject = async (data: ProyectoCreate) => {
    try {
      const newProject = await proyectoService.create(data);
      setProyectos((prev) => [...prev, newProject]);
    } catch (err) {
      console.error('Error creating project:', err);
      throw err;
    }
  };

  return (
    <div className="p-8">
      {/* Main Container */}
      <div className="max-w-5xl mx-auto flex flex-col items-center pb-12 mt-6">
        {/* Title / Mascot - Made MASSIVE */}
        <div className="mb-0 z-20 relative">
          <img 
            src={tituloPlanovaImg}
            alt="Planova Title" 
            className="w-[45rem] max-w-[90vw] object-contain drop-shadow-[0_15px_30px_rgba(0,0,0,0.6)] translate-y-16"
          />
        </div>

        {/* The Giant Project Panel */}
        <div className="w-full relative z-10 px-4">
          <div className="glass-extreme w-full flex flex-col pt-[5.5rem] pb-8 px-6 md:px-10">
            {/* Header: PROYECTOS */}
            <div className="w-full flex justify-center mb-10">
              <div className="w-full max-w-2xl border-b-[1px] border-white/30 pb-3 text-center">
                <h2 className="text-[1.7rem] text-white tracking-[0.2em] font-light">
                  PROYECTOS
                </h2>
              </div>
            </div>

      {/* Project Grid */}
      <div className="w-full px-4">
        {isLoading ? (
          <div className="text-center text-white/70 py-10">Cargando proyectos...</div>
        ) : error ? (
          <div className="text-center text-red-300 py-10">{error}</div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {/* Project Cards */}
            {proyectos.map((proyecto) => (
              <ProjectCard
                key={proyecto.id}
                id={proyecto.id}
                nombre={proyecto.nombre}
              />
            ))}
            
            {/* Create New Project Card */}
            <div 
              className="flex items-center justify-center cursor-pointer transition-all hover:bg-white/10 rounded-2xl group"
              onClick={() => setIsModalOpen(true)}
              style={{
                border: '1.5px solid rgba(255,255,255,0.4)',
                background: 'rgba(255,255,255,0.03)',
                boxShadow: 'inset 0 0 10px rgba(255,255,255,0.1), 0 5px 15px rgba(0,0,0,0.3)',
                minHeight: '120px'
              }}
            >
              <span className="text-white/60 text-[5rem] font-light group-hover:text-white transition-colors" style={{ lineHeight: '0' }}>+</span>
            </div>
          </div>
        )}
      </div>
          </div>
        </div>
      </div>

      {/* Create Project Modal */}
      <CreateProjectModal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        onSubmit={handleCreateProject}
      />
    </div>
  );
};

export default HomePage;