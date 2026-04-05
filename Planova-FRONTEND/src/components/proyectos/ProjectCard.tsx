/**
 * ProjectCard Component for Planova
 * Glass card showing project info with hover effects
 */

import React from 'react';
import { useNavigate } from 'react-router-dom';

interface ProjectCardProps {
  id: number;
  nombre: string;
  fechaCreacion?: string;
}

export const ProjectCard: React.FC<ProjectCardProps> = ({
  id,
  nombre,
  fechaCreacion,
}) => {
  const navigate = useNavigate();

  const handleClick = () => {
    navigate(`/proyecto/${id}`);
  };

  // Format the date if available
  const formattedDate = fechaCreacion 
    ? new Date(fechaCreacion).toLocaleDateString('es-AR', {
        day: 'numeric',
        month: 'short',
        year: 'numeric'
      })
    : null;

  return (
    <div 
      className="p-5 cursor-pointer flex flex-col justify-center transition-all hover:bg-white/10 hover:scale-[1.02]"
      onClick={handleClick}
      style={{
        border: '1.5px solid rgba(255,255,255,0.4)',
        background: 'rgba(255,255,255,0.03)',
        boxShadow: 'inset 0 0 15px rgba(255,255,255,0.15), 0 5px 15px rgba(0,0,0,0.3)',
        borderRadius: '16px',
        minHeight: '120px'
      }}
    >
      <h3 className="text-[1.6rem] text-white mb-2 leading-none truncate w-full" style={{ fontFamily: 'Kalam, cursive' }} title={nombre}>
        {nombre}
      </h3>

      <div className="text-white/60 text-[0.95rem] flex flex-col leading-tight" style={{ fontFamily: 'Kalam, cursive' }}>
        <span className="opacity-90">fecha creacion {formattedDate ? `- ${formattedDate}` : ''}</span>
        <span className="opacity-70 mt-0.5">cantidad de tareas {Math.floor(Math.random() * 5)}</span>
      </div>
    </div>
  );
};

export default ProjectCard;