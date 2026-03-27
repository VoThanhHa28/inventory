import React from 'react'

interface FormInputProps extends React.InputHTMLAttributes<HTMLInputElement> {
  label?: string
  error?: string
  helperText?: string
  required?: boolean
}

/**
 * Reusable Form Input Component
 * With label, error message, and helper text
 */
const FormInput = React.forwardRef<HTMLInputElement, FormInputProps>(
  ({ label, error, helperText, required, className, ...props }, ref) => {
    return (
      <div className="w-full">
        {label && (
          <label className="block text-sm font-medium text-gray-700 mb-1">
            {label}
            {required && <span className="text-red-500 ml-1">*</span>}
          </label>
        )}
        <input
          ref={ref}
          className={`w-full px-4 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 transition ${
            error ? 'border-red-500' : 'border-gray-300'
          } disabled:bg-gray-100 disabled:text-gray-500 ${className || ''}`}
          {...props}
        />
        {error && <p className="text-red-500 text-sm mt-1">{error}</p>}
        {helperText && !error && <p className="text-gray-500 text-sm mt-1">{helperText}</p>}
      </div>
    )
  }
)

FormInput.displayName = 'FormInput'

export default FormInput
