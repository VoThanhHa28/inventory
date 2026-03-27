import React, { useState, useMemo } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import { useMutation } from '@tanstack/react-query'
import authApi from '@api/auth'
import { validateEmail, validatePassword, validateUsername } from '@utils/validators'
import { ApiError } from '@types'

interface FormErrors {
  username?: string
  email?: string
  password?: string
  confirmPassword?: string
  submit?: string
}

interface PasswordStrength {
  level: 'weak' | 'fair' | 'good' | 'strong'
  percentage: number
  color: string
}

/**
 * Calculate password strength
 */
const calculatePasswordStrength = (password: string): PasswordStrength => {
  if (!password) return { level: 'weak', percentage: 0, color: 'bg-gray-300' }

  let strength = 0
  let percentage = 25

  if (password.length >= 8) strength++
  if (password.length >= 12) strength++
  if (/[A-Z]/.test(password)) strength++
  if (/[a-z]/.test(password)) strength++
  if (/[0-9]/.test(password)) strength++
  if (/[^A-Za-z0-9]/.test(password)) strength++

  if (strength <= 2) {
    return { level: 'weak', percentage: 25, color: 'bg-red-500' }
  } else if (strength <= 3) {
    return { level: 'fair', percentage: 50, color: 'bg-yellow-500' }
  } else if (strength <= 4) {
    return { level: 'good', percentage: 75, color: 'bg-blue-500' }
  } else {
    return { level: 'strong', percentage: 100, color: 'bg-green-500' }
  }
}

/**
 * Register Page
 * New user registration with email, username, password validation
 * Password strength indicator shows real-time feedback
 * On success: stores JWT token + user in Zustand + redirects to /shop/products
 */
const RegisterPage: React.FC = () => {
  const navigate = useNavigate()
  const [username, setUsername] = useState('')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [errors, setErrors] = useState<FormErrors>({})
  const [showPassword, setShowPassword] = useState(false)
  const [showConfirmPassword, setShowConfirmPassword] = useState(false)

  // Calculate password strength in real-time
  const passwordStrength = useMemo(() => calculatePasswordStrength(password), [password])

  // Register mutation with React Query
  const registerMutation = useMutation({
    mutationFn: () =>
      authApi.register({
        username,
        email,
        password,
      }),
    onSuccess: () => {
      // Clear form
      setUsername('')
      setEmail('')
      setPassword('')
      setConfirmPassword('')
      setErrors({})
      // Redirect to products page
      navigate('/shop/products')
    },
    onError: (error: any) => {
      const apiError = error.response?.data as ApiError | undefined
      setErrors({
        submit: apiError?.message || 'Registration failed. Please try again.',
      })
    },
  })

  /**
   * Validate form before submission
   */
  const validateForm = (): boolean => {
    const newErrors: FormErrors = {}

    // Username validation
    if (!username) {
      newErrors.username = 'Username is required'
    } else if (!validateUsername(username)) {
      newErrors.username = 'Username must be 3-20 characters (letters, numbers, -, _)'
    }

    // Email validation
    if (!email) {
      newErrors.email = 'Email is required'
    } else if (!validateEmail(email)) {
      newErrors.email = 'Invalid email address'
    }

    // Password validation
    if (!password) {
      newErrors.password = 'Password is required'
    } else {
      const passwordValidation = validatePassword(password)
      if (!passwordValidation.isValid) {
        newErrors.password = passwordValidation.errors[0] // Show first error
      }
    }

    // Confirm password
    if (!confirmPassword) {
      newErrors.confirmPassword = 'Please confirm your password'
    } else if (password !== confirmPassword) {
      newErrors.confirmPassword = 'Passwords do not match'
    }

    setErrors(newErrors)
    return Object.keys(newErrors).length === 0
  }

  /**
   * Handle form submission
   */
  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    if (validateForm()) {
      registerMutation.mutate()
    }
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-100 py-12">
      <div className="bg-white rounded-lg shadow-lg p-8 max-w-md w-full">
        {/* Header */}
        <h1 className="text-3xl font-bold text-center text-gray-800 mb-2">Create Account</h1>
        <p className="text-center text-gray-600 mb-6">Join us today</p>

        {/* Error Message */}
        {errors.submit && (
          <div className="mb-4 p-3 bg-red-100 border border-red-400 text-red-700 rounded">
            {errors.submit}
          </div>
        )}

        {/* Form */}
        <form onSubmit={handleSubmit} className="space-y-4">
          {/* Username Field */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Username
            </label>
            <input
              type="text"
              value={username}
              onChange={(e) => {
                setUsername(e.target.value)
                if (errors.username) setErrors({ ...errors, username: undefined })
              }}
              placeholder="johndoe"
              className={`w-full px-4 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 ${
                errors.username ? 'border-red-500' : 'border-gray-300'
              }`}
              disabled={registerMutation.isPending}
            />
            {errors.username && <p className="text-red-500 text-sm mt-1">{errors.username}</p>}
          </div>

          {/* Email Field */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Email Address
            </label>
            <input
              type="email"
              value={email}
              onChange={(e) => {
                setEmail(e.target.value)
                if (errors.email) setErrors({ ...errors, email: undefined })
              }}
              placeholder="you@example.com"
              className={`w-full px-4 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 ${
                errors.email ? 'border-red-500' : 'border-gray-300'
              }`}
              disabled={registerMutation.isPending}
            />
            {errors.email && <p className="text-red-500 text-sm mt-1">{errors.email}</p>}
          </div>

          {/* Password Field */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Password
            </label>
            <div className="relative">
              <input
                type={showPassword ? 'text' : 'password'}
                value={password}
                onChange={(e) => {
                  setPassword(e.target.value)
                  if (errors.password) setErrors({ ...errors, password: undefined })
                }}
                placeholder="••••••••"
                className={`w-full px-4 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 ${
                  errors.password ? 'border-red-500' : 'border-gray-300'
                }`}
                disabled={registerMutation.isPending}
              />
              <button
                type="button"
                onClick={() => setShowPassword(!showPassword)}
                className="absolute right-3 top-2.5 text-gray-500 hover:text-gray-700"
              >
                {showPassword ? '🙈' : '👁️'}
              </button>
            </div>

            {/* Password Strength Indicator */}
            {password && (
              <div className="mt-2">
                <div className="flex justify-between items-center mb-1">
                  <span className="text-xs font-medium text-gray-600">Strength:</span>
                  <span
                    className={`text-xs font-semibold capitalize ${
                      passwordStrength.level === 'weak'
                        ? 'text-red-600'
                        : passwordStrength.level === 'fair'
                          ? 'text-yellow-600'
                          : passwordStrength.level === 'good'
                            ? 'text-blue-600'
                            : 'text-green-600'
                    }`}
                  >
                    {passwordStrength.level}
                  </span>
                </div>
                <div className="w-full bg-gray-200 rounded-full h-2">
                  <div
                    className={`h-2 rounded-full transition-all ${passwordStrength.color}`}
                    style={{ width: `${passwordStrength.percentage}%` }}
                  ></div>
                </div>
              </div>
            )}

            {errors.password && <p className="text-red-500 text-sm mt-1">{errors.password}</p>}

            {/* Password Requirements */}
            {password && (
              <div className="mt-2 p-2 bg-gray-50 rounded border border-gray-200 text-xs text-gray-600">
                <p>✓ At least 6 characters long</p>
                <p>✓ Uppercase letter (A-Z)</p>
                <p>✓ Lowercase letter (a-z)</p>
                <p>✓ Number (0-9)</p>
              </div>
            )}
          </div>

          {/* Confirm Password Field */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Confirm Password
            </label>
            <div className="relative">
              <input
                type={showConfirmPassword ? 'text' : 'password'}
                value={confirmPassword}
                onChange={(e) => {
                  setConfirmPassword(e.target.value)
                  if (errors.confirmPassword)
                    setErrors({ ...errors, confirmPassword: undefined })
                }}
                placeholder="••••••••"
                className={`w-full px-4 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 ${
                  errors.confirmPassword ? 'border-red-500' : 'border-gray-300'
                }`}
                disabled={registerMutation.isPending}
              />
              <button
                type="button"
                onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                className="absolute right-3 top-2.5 text-gray-500 hover:text-gray-700"
              >
                {showConfirmPassword ? '🙈' : '👁️'}
              </button>
            </div>
            {errors.confirmPassword && (
              <p className="text-red-500 text-sm mt-1">{errors.confirmPassword}</p>
            )}
          </div>

          {/* Submit Button */}
          <button
            type="submit"
            disabled={registerMutation.isPending}
            className="w-full py-2 px-4 bg-blue-600 text-white font-medium rounded-lg hover:bg-blue-700 disabled:bg-gray-400 disabled:cursor-not-allowed transition"
          >
            {registerMutation.isPending ? 'Creating account...' : 'Create Account'}
          </button>
        </form>

        {/* Footer Links */}
        <p className="text-center text-gray-600 text-sm mt-6">
          Already have an account?{' '}
          <Link to="/auth/login" className="text-blue-600 hover:underline font-medium">
            Sign in
          </Link>
        </p>
      </div>
    </div>
  )
}

export default RegisterPage
