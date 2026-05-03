import React from 'react';

interface SkeletonBlockProps {
  className?: string;
}

const SkeletonBlock: React.FC<SkeletonBlockProps> = ({ className = '' }) => {
  return <div className={`animate-pulse rounded-2xl bg-slate-200/70 ${className}`.trim()} />;
};

export default SkeletonBlock;
