import React from 'react'

interface SkeletonProps {
  width?: string
  height?: string
  className?: string
  count?: number
  circle?: boolean
}

/**
 * Skeleton Loading Component
 * Shows animated placeholder while loading content
 */
const Skeleton: React.FC<SkeletonProps> = ({
  width = 'w-full',
  height = 'h-4',
  className = '',
  count = 1,
  circle = false,
}) => {
  const baseClass = 'bg-gray-200 animate-pulse rounded'
  const circleClass = circle ? 'rounded-full' : ''

  return (
    <>
      {Array.from({ length: count }).map((_, index) => (
        <div
          key={index}
          className={`${baseClass} ${circleClass} ${width} ${height} ${className} ${index < count - 1 ? 'mb-3' : ''}`}
        />
      ))}
    </>
  )
}

/**
 * Skeleton for Product Card
 */
export const ProductCardSkeleton: React.FC = () => {
  return (
    <div className="bg-white rounded-lg shadow p-4 space-y-4">
      <Skeleton height="h-48" className="rounded" />
      <Skeleton height="h-6" width="w-3/4" />
      <Skeleton height="h-4" width="w-1/2" />
      <div className="flex gap-2 pt-2">
        <Skeleton height="h-10" width="w-1/2" className="rounded" />
        <Skeleton height="h-10" width="w-1/2" className="rounded" />
      </div>
    </div>
  )
}

/**
 * Skeleton for List Items
 */
export const ListItemSkeleton: React.FC = () => {
  return (
    <div className="bg-white rounded-lg p-4 mb-4">
      <div className="flex gap-4">
        <Skeleton height="h-12" width="w-12" circle />
        <div className="flex-1 space-y-2">
          <Skeleton height="h-5" width="w-3/4" />
          <Skeleton height="h-4" width="w-1/2" />
        </div>
      </div>
    </div>
  )
}

/**
 * Skeleton for Detailed Content
 */
export const ContentSkeleton: React.FC = () => {
  return (
    <div className="space-y-4">
      <Skeleton height="h-64" className="rounded" />
      <Skeleton height="h-8" width="w-1/2" />
      <Skeleton height="h-4" count={3} />
      <div className="flex gap-4 pt-4">
        <Skeleton height="h-10" width="w-1/3" className="rounded" />
        <Skeleton height="h-10" width="w-1/3" className="rounded" />
      </div>
    </div>
  )
}

export default Skeleton
