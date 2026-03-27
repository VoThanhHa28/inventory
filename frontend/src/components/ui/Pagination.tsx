import React from 'react'

interface PaginationProps {
  currentPage: number
  totalPages: number
  onPageChange: (page: number) => void
  disabled?: boolean
}

/**
 * Pagination Component
 * Handles navigation between pages
 */
const Pagination: React.FC<PaginationProps> = ({
  currentPage,
  totalPages,
  onPageChange,
  disabled = false,
}) => {
  const canGoPrevious = currentPage > 1
  const canGoNext = currentPage < totalPages

  const pages = []
  const maxVisible = 5
  let startPage = Math.max(1, currentPage - Math.floor(maxVisible / 2))
  let endPage = Math.min(totalPages, startPage + maxVisible - 1)

  if (endPage - startPage < maxVisible - 1) {
    startPage = Math.max(1, endPage - maxVisible + 1)
  }

  if (startPage > 1) {
    pages.push(1)
    if (startPage > 2) pages.push(-1) // -1 represents ellipsis
  }

  for (let i = startPage; i <= endPage; i++) {
    pages.push(i)
  }

  if (endPage < totalPages) {
    if (endPage < totalPages - 1) pages.push(-1)
    pages.push(totalPages)
  }

  return (
    <div className="flex items-center justify-center gap-2 mt-8">
      <button
        onClick={() => onPageChange(currentPage - 1)}
        disabled={!canGoPrevious || disabled}
        className="px-3 py-2 border rounded-lg hover:bg-gray-100 disabled:opacity-50 disabled:cursor-not-allowed"
      >
        ← Previous
      </button>

      <div className="flex gap-1">
        {pages.map((page, index) => {
          if (page === -1) {
            return (
              <span key={`ellipsis-${index}`} className="px-2 py-2">
                ...
              </span>
            )
          }

          const isActive = page === currentPage

          return (
            <button
              key={page}
              onClick={() => onPageChange(page as number)}
              disabled={disabled}
              className={`px-3 py-2 border rounded-lg ${
                isActive
                  ? 'bg-blue-600 text-white border-blue-600'
                  : 'hover:bg-gray-100'
              } disabled:cursor-not-allowed`}
            >
              {page}
            </button>
          )
        })}
      </div>

      <button
        onClick={() => onPageChange(currentPage + 1)}
        disabled={!canGoNext || disabled}
        className="px-3 py-2 border rounded-lg hover:bg-gray-100 disabled:opacity-50 disabled:cursor-not-allowed"
      >
        Next →
      </button>

      <div className="ml-4 text-sm text-gray-600">
        Page {currentPage} of {totalPages}
      </div>
    </div>
  )
}

export default Pagination
