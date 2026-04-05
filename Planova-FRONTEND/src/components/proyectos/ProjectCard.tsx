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
  tareaCount?: number;
  onEdit?: (id: number) => void;
}

export const ProjectCard: React.FC<ProjectCardProps> = ({
  id,
  nombre,
  fechaCreacion,
  tareaCount = 0,
  onEdit,
}) => {
  const navigate = useNavigate();

  const handleClick = () => {
    navigate(`/proyecto/${id}`);
  };

  const handleEdit = (e: React.MouseEvent) => {
    e.stopPropagation();
    onEdit?.(id);
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
      className="p-5 cursor-pointer flex flex-col justify-center transition-all hover:bg-white/10 hover:scale-[1.02] relative group"
      onClick={handleClick}
      style={{
        border: '1.5px solid rgba(255,255,255,0.4)',
        background: 'rgba(255,255,255,0.03)',
        boxShadow: 'inset 0 0 15px rgba(255,255,255,0.15), 0 5px 15px rgba(0,0,0,0.3)',
        borderRadius: '16px',
        minHeight: '120px'
      }}
    >
      {/* Edit Button */}
      <button
        onClick={handleEdit}
        className="absolute top-3 right-3 p-2 text-white/30 hover:text-white hover:bg-white/10 rounded-lg opacity-0 group-hover:opacity-100 transition-all z-10"
        title="Editar proyecto"
      >
        <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15.232 5.232l3.536 3.536m-2.036-5.036a2.5 2.5 0 113.536 3.536L6.5 21.036H3v-3.572L16.732 3.732z" />
        </svg>
      </button>

      <h3 className="text-[1.6rem] text-white mb-2 leading-none truncate w-full pr-8" style={{ fontFamily: 'Kalam, cursive' }} title={nombre}>
        {nombre}
      </h3>

      <div className="text-white/60 text-[0.95rem] flex flex-col leading-tight" style={{ fontFamily: 'Kalam, cursive' }}>
        <span className="opacity-90">fecha creacion {formattedDate ? `- ${formattedDate}` : ''}</span>
        <span className="opacity-70 mt-0.5">cantidad de tareas - {tareaCount}</span>
      </div>
    </div>
  );
};

export default ProjectCard;