import React, { useMemo } from 'react';

interface GlassSparklesProps {
  count?: number;
}

/**
 * Reusable component to add shimmering particles (sparkles) 
 * to glass containers.
 */
export const GlassSparkles: React.FC<GlassSparklesProps> = ({ count = 6 }) => {
  const sparkles = useMemo(() => {
    return Array.from({ length: count }).map((_, i) => ({
      id: i,
      top: `${Math.random() * 90 + 5}%`,
      left: `${Math.random() * 90 + 5}%`,
      duration: `${Math.random() * 3 + 2}s`,
      delay: `${Math.random() * 2}s`,
    }));
  }, [count]);

  return (
    <div className="glass-sparkles" aria-hidden="true">
      {sparkles.map((s) => (
        <div
          key={s.id}
          className="glass-sparkle"
          style={{
            top: s.top,
            left: s.left,
            '--duration': s.duration,
            '--delay': s.delay,
          } as React.CSSProperties}
        />
      ))}
    </div>
  );
};

export default GlassSparkles;
