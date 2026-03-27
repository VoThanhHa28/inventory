import React from 'react'

interface ButtonProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: 'primary' | 'secondary' | 'danger' | 'success'
  size?: 'sm' | 'md' | 'lg'
  isLoading?: boolean
  children: React.ReactNode
}

/**
 * Reusable Button Component
 * Consistent styling across the app
 */
const Button = React.forwardRef<HTMLButtonElement, ButtonProps>(
  (
    {
      variant = 'primary',
      size = 'md',
      isLoading = false,
      children,
      disabled,
      className,
      ...props
    },
    ref
  ) => {
    // Base styles
    const baseStyles = 'rounded-lg font-medium transition focus:outline-none focus:ring-2'

    // Variant styles
    const variantStyles = {
      primary: 'bg-blue-600 text-white hover:bg-blue-700 focus:ring-blue-500 disabled:bg-gray-400',
      secondary: 'bg-gray-200 text-gray-800 hover:bg-gray-300 focus:ring-gray-400 disabled:bg-gray-300',
      danger: 'bg-red-600 text-white hover:bg-red-700 focus:ring-red-500 disabled:bg-gray-400',
      success:
        'bg-green-600 text-white hover:bg-green-700 focus:ring-green-500 disabled:bg-gray-400',
    }

    // Size styles
    const sizeStyles = {
      sm: 'px-3 py-1 text-sm',
      md: 'px-4 py-2 text-base',
      lg: 'px-6 py-3 text-lg',
    }

    // Combine all styles
    const allStyles = `${baseStyles} ${variantStyles[variant]} ${sizeStyles[size]} ${className || ''}`

    return (
      <button
        ref={ref}
        disabled={disabled || isLoading}
        className={`${allStyles} disabled:cursor-not-allowed`}
        {...props}
      >
        {isLoading ? (
          <span className="flex items-center gap-2">
            <span className="inline-block w-4 h-4 border-2 border-current border-t-transparent rounded-full animate-spin"></span>
            {children}
          </span>
        ) : (
          children
        )}
      </button>
    )
  }
)

Button.displayName = 'Button'

export default Button
