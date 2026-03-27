import React, { useState } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import { useMutation } from '@tanstack/react-query'
import authApi from '@api/auth'
import { validatePassword } from '@utils/validators'
import { ApiError } from '@types'

interface FormErrors {
  username?: string
  password?: string
  submit?: string
}

/**
 * Login Page
 * Handles user authentication with username and password
 * On success: stores JWT token + user in Zustand + redirects to /shop/products
 */
const LoginPage: React.FC = () => {
  const navigate = useNavigate()
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [errors, setErrors] = useState<FormErrors>({})
  const [showPassword, setShowPassword] = useState(false)

  // Login mutation with React Query
  const loginMutation = useMutation({
    mutationFn: () => authApi.login({ username, password }),
    onSuccess: () => {
      // Clear form
      setUsername('')
      setPassword('')
      setErrors({})
      // Redirect to products page
      navigate('/shop/products')
    },
    onError: (error: any) => {
      const apiError = error.response?.data as ApiError | undefined
      const errorMessage = apiError?.message || 'Login failed. Please try again.'
      
      // Convert Spring Security error to user-friendly message
      let displayMessage = errorMessage
      if (errorMessage.includes('Bad Credentials') || errorMessage.includes('Unauthorized')) {
        displayMessage = 'Invalid username or password'
      }
      
      setErrors({
        submit: displayMessage,
      })
    },
  })

  /**
   * Validate form before submission
   */
  const validateForm = (): boolean => {
    const newErrors: FormErrors = {}

    if (!username) {
      newErrors.username = 'Username is required'
    }

    if (!password) {
      newErrors.password = 'Password is required'
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
      loginMutation.mutate()
    }
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-100">
      <div className="bg-white rounded-lg shadow-lg p-8 max-w-md w-full">
        {/* Header */}
        <h1 className="text-3xl font-bold text-center text-gray-800 mb-2">Login</h1>
        <p className="text-center text-gray-600 mb-6">Sign in to your account</p>

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
              placeholder="admin3, user1, or user2"
              className={`w-full px-4 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 ${
                errors.username ? 'border-red-500' : 'border-gray-300'
              }`}
              disabled={loginMutation.isPending}
            />
            {errors.username && <p className="text-red-500 text-sm mt-1">{errors.username}</p>}
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
                disabled={loginMutation.isPending}
              />
              <button
                type="button"
                onClick={() => setShowPassword(!showPassword)}
                className="absolute right-3 top-2.5 text-gray-500 hover:text-gray-700"
              >
                {showPassword ? '🙈' : '👁️'}
              </button>
            </div>
            {errors.password && <p className="text-red-500 text-sm mt-1">{errors.password}</p>}
          </div>

          {/* Submit Button */}
          <button
            type="submit"
            disabled={loginMutation.isPending}
            className="w-full py-2 px-4 bg-blue-600 text-white font-medium rounded-lg hover:bg-blue-700 disabled:bg-gray-400 disabled:cursor-not-allowed transition"
          >
            {loginMutation.isPending ? 'Signing in...' : 'Sign In'}
          </button>
        </form>

        {/* Divider */}
        <div className="my-6 flex items-center">
          <div className="flex-1 border-t border-gray-300"></div>
          <div className="px-2 text-gray-500 text-sm">or</div>
          <div className="flex-1 border-t border-gray-300"></div>
        </div>

        {/* Footer Links */}
        <p className="text-center text-gray-600 text-sm mb-3">
          Don't have an account?{' '}
          <Link to="/auth/register" className="text-blue-600 hover:underline font-medium">
            Sign up
          </Link>
        </p>

        <p className="text-center text-gray-600 text-sm">
          <a href="#" className="text-blue-600 hover:underline">
            Forgot password?
          </a>
        </p>
      </div>
    </div>
  )
}

export default LoginPage
