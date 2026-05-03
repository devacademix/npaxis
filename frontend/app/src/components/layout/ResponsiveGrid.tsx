import React from 'react';

interface ResponsiveGridProps {
  children: React.ReactNode;
  mobileCols?: 1 | 2;
  tabletCols?: 1 | 2 | 3;
  desktopCols?: 1 | 2 | 3 | 4;
  className?: string;
}

const mobileColsMap = {
  1: 'grid-cols-1',
  2: 'grid-cols-2',
};

const tabletColsMap = {
  1: 'sm:grid-cols-1',
  2: 'sm:grid-cols-2',
  3: 'sm:grid-cols-3',
};

const desktopColsMap = {
  1: 'lg:grid-cols-1',
  2: 'lg:grid-cols-2',
  3: 'lg:grid-cols-3',
  4: 'lg:grid-cols-4',
};

const ResponsiveGrid: React.FC<ResponsiveGridProps> = ({
  children,
  mobileCols = 1,
  tabletCols = 2,
  desktopCols = 3,
  className = '',
}) => {
  return (
    <div
      className={`grid gap-4 ${mobileColsMap[mobileCols]} ${tabletColsMap[tabletCols]} ${desktopColsMap[desktopCols]} ${className}`.trim()}
    >
      {children}
    </div>
  );
};

export default ResponsiveGrid;
