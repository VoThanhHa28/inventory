import React, { useState } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import { useMutation } from '@tanstack/react-query'
import authApi from '@api/auth'
import { validateEmail, validatePassword, validateUsername } from '@utils/validators'
import { ApiError } from '@types'

interface FormErrorsInterface {
  username?: string
  fullName?: string
  email?: string
  password?: string
  confirmPassword?: string
  agreeToTerms?: string
  submit?: string
}



const RegisterPage: React.FC = () => {
  const navigate = useNavigate()
  const [formData, setFormData] = useState({
    username: '',
    fullName: '',
    email: '',
    password: '',
    confirmPassword: '',
    agreeToTerms: false,
  })
  const [errors, setErrors] = useState<FormErrorsInterface>({})
  const [showPassword, setShowPassword] = useState(false)
  const [showConfirmPassword, setShowConfirmPassword] = useState(false)

  const registerMutation = useMutation({
    mutationFn: () =>
      authApi.register({
        username: formData.username,
        fullName: formData.fullName,
        email: formData.email,
        password: formData.password,
      }),
    onSuccess: () => {
      navigate('/auth/login')
    },
    onError: (error: any) => {
      const apiError = error.response?.data as ApiError | undefined
      const errorMessage = apiError?.message || 'Registration failed. Please try again.'
      setErrors({ submit: errorMessage })
    },
  })

  const validateForm = (): boolean => {
    const newErrors: FormErrorsInterface = {}

    // Username validation
    if (!formData.username || formData.username.length < 3) {
      newErrors.username = 'Username must be at least 3 characters'
    }

    // Full name validation
    if (!formData.fullName || formData.fullName.length < 2) {
      newErrors.fullName = 'Full name is required'
    }

    // Email validation
    if (!formData.email || !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.email)) {
      newErrors.email = 'Valid email is required'
    }

    // Password validation
    if (!formData.password || formData.password.length < 6) {
      newErrors.password = 'Password must be at least 6 characters'
    }

    // Confirm password validation
    if (formData.password !== formData.confirmPassword) {
      newErrors.confirmPassword = 'Passwords do not match'
    }

    // Terms validation
    if (!formData.agreeToTerms) {
      newErrors.agreeToTerms = 'You must agree to the terms'
    }

    setErrors(newErrors)
    return Object.keys(newErrors).length === 0
  }

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value, type, checked } = e.target
    setFormData((prev) => ({
      ...prev,
      [name]: type === 'checkbox' ? checked : value,
    }))
    if (errors[name as keyof FormErrorsInterface]) {
      setErrors({
        ...errors,
        [name]: undefined,
      })
    }
  }

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    if (validateForm()) {
      registerMutation.mutate()
    }
  }

  return (
    <div className="bg-surface font-body text-on-surface min-h-screen flex flex-col">
      {/* TopNavBar - Fixed */}
      <nav className="fixed top-0 w-full z-50 bg-surface/70 backdrop-blur-xl border-b border-surface-container">
        <div className="flex justify-between items-center px-8 py-4">
          <div className="flex items-center gap-2">
            <span className="material-symbols-outlined text-primary text-2xl">inventory_2</span>
            <span className="text-xl font-bold tracking-tight text-primary font-headline">InventoryApp</span>
          </div>
          <div className="flex items-center gap-6">
            <Link to="/auth/login" className="text-on-surface-variant font-medium hover:bg-surface-container-low px-4 py-2 rounded-lg transition-colors duration-300">
              Login
            </Link>
            <Link to="/auth/register" className="bg-signature-gradient text-white font-semibold px-6 py-2 rounded-lg hover:opacity-90 transition-all duration-200 transform hover:scale-[0.98] active:scale-95">
              Register
            </Link>
          </div>
        </div>
      </nav>

      {/* Main Content - pt-16 to offset fixed navbar */}
      <main className="flex-grow flex pt-16">
        <div className="w-full flex">
        {/* LEFT: Image & Branding Section */}
        <div className="hidden lg:flex lg:w-1/2 relative bg-gradient-to-br from-primary/20 via-primary/10 to-surface-container-low overflow-hidden items-center justify-center p-12">
          {/* Gradient Overlay with Pattern */}
          <div className="absolute inset-0 z-0">
            {/* Base gradient layers */}
            <div className="absolute inset-0 bg-gradient-to-tr from-primary/15 via-transparent to-primary/5"></div>
            {/* Pattern overlay */}
            <div className="absolute inset-0 opacity-40" style={{
              backgroundImage: `radial-gradient(circle at 20% 80%, rgba(0, 74, 198, 0.1) 0%, transparent 50%), 
                                radial-gradient(circle at 80% 20%, rgba(37, 99, 235, 0.1) 0%, transparent 50%)`
            }}></div>
          </div>

          {/* Content Overlay */}
          <div className="relative z-10 max-w-lg">
            {/* Industry Standard Badge */}
            <div className="mb-8 inline-flex items-center gap-3 bg-white/80 glass-effect p-3 px-5 rounded-3xl shadow-sm border border-white/20">
              <span className="material-symbols-outlined text-primary" style={{ fontVariationSettings: "'FILL' 1" }}>verified</span>
              <span className="text-sm font-semibold text-on-surface-variant font-label uppercase tracking-widest">Industry Standard</span>
            </div>

            {/* Hero Headline */}
            <h1 className="text-5xl font-extrabold font-headline leading-tight text-on-surface mb-6 tracking-tight">
              Streamline your <span className="text-primary">inventory</span> operations.
            </h1>

            {/* Description */}
            <p className="text-xl text-on-surface-variant leading-relaxed font-body mb-10">
              Experience the executive dashboard designed for absolute control over your global inventory ecosystem.
            </p>

            {/* Bento Grid - 2 Feature Cards */}
            <div className="grid grid-cols-2 gap-4">
              <div className="bg-surface-container-lowest p-6 rounded-2xl shadow-sm border border-outline-variant/10">
                <span className="material-symbols-outlined text-primary mb-3 block">security</span>
                <div className="font-headline font-bold text-on-surface">Secure Data</div>
              </div>
              <div className="bg-surface-container-lowest p-6 rounded-2xl shadow-sm border border-outline-variant/10">
                <span className="material-symbols-outlined text-primary mb-3 block">quick_reference</span>
                <div className="font-headline font-bold text-on-surface">Quick Setup</div>
              </div>
            </div>
          </div>
        </div>

        {/* RIGHT: Register Form Section */}
        <div className="w-full lg:w-1/2 flex items-center justify-center p-6 sm:p-12 md:p-20 bg-surface">
          <div className="w-full max-w-md">
            {/* Form Header */}
            <div className="mb-10 text-center lg:text-left">
              <h2 className="text-3xl font-extrabold font-headline text-on-surface tracking-tight mb-2">Create your account</h2>
              <p className="text-on-surface-variant font-body">Join thousands of businesses managing inventory with InventoryApp.</p>
            </div>

            {/* Error Message */}
            {errors.submit && (
              <div className="mb-6 p-4 bg-red-50/50 border border-red-200/50 text-red-700 rounded-xl text-sm">
                ⚠️ {errors.submit}
              </div>
            )}

            {/* Form */}
            <form onSubmit={handleSubmit} className="space-y-6">
              {/* Username Field */}
              <div className="space-y-2">
                <label htmlFor="username" className="block text-sm font-semibold font-label text-on-surface-variant uppercase tracking-wider">
                  Username
                </label>
                <div className="relative">
                  <span className="absolute left-4 top-1/2 -translate-y-1/2 material-symbols-outlined text-outline">person</span>
                  <input
                    id="username"
                    type="text"
                    name="username"
                    value={formData.username}
                    onChange={handleChange}
                    placeholder="johndoe"
                    className="w-full pl-12 pr-4 py-4 bg-surface-container-low border-none rounded-xl focus:ring-2 focus:ring-primary/20 focus:bg-surface-container-lowest transition-all duration-200 text-on-surface placeholder:text-outline"
                    disabled={registerMutation.isPending}
                  />
                </div>
                {errors.username && (
                  <p className="text-red-600 text-sm font-medium">{errors.username}</p>
                )}
              </div>

              {/* Full Name Field */}
              <div className="space-y-2">
                <label htmlFor="fullName" className="block text-sm font-semibold font-label text-on-surface-variant uppercase tracking-wider">
                  Full Name
                </label>
                <div className="relative">
                  <span className="absolute left-4 top-1/2 -translate-y-1/2 material-symbols-outlined text-outline">badge</span>
                  <input
                    id="fullName"
                    type="text"
                    name="fullName"
                    value={formData.fullName}
                    onChange={handleChange}
                    placeholder="John Doe"
                    className="w-full pl-12 pr-4 py-4 bg-surface-container-low border-none rounded-xl focus:ring-2 focus:ring-primary/20 focus:bg-surface-container-lowest transition-all duration-200 text-on-surface placeholder:text-outline"
                    disabled={registerMutation.isPending}
                  />
                </div>
                {errors.fullName && (
                  <p className="text-red-600 text-sm font-medium">{errors.fullName}</p>
                )}
              </div>

              {/* Email Field */}
              <div className="space-y-2">
                <label htmlFor="email" className="block text-sm font-semibold font-label text-on-surface-variant uppercase tracking-wider">
                  Email Address
                </label>
                <div className="relative">
                  <span className="absolute left-4 top-1/2 -translate-y-1/2 material-symbols-outlined text-outline">mail</span>
                  <input
                    id="email"
                    type="email"
                    name="email"
                    value={formData.email}
                    onChange={handleChange}
                    placeholder="john@company.com"
                    className="w-full pl-12 pr-4 py-4 bg-surface-container-low border-none rounded-xl focus:ring-2 focus:ring-primary/20 focus:bg-surface-container-lowest transition-all duration-200 text-on-surface placeholder:text-outline"
                    disabled={registerMutation.isPending}
                  />
                </div>
                {errors.email && (
                  <p className="text-red-600 text-sm font-medium">{errors.email}</p>
                )}
              </div>

              {/* Password Field */}
              <div className="space-y-2">
                <label htmlFor="password" className="block text-sm font-semibold font-label text-on-surface-variant uppercase tracking-wider">
                  Password
                </label>
                <div className="relative">
                  <span className="absolute left-4 top-1/2 -translate-y-1/2 material-symbols-outlined text-outline">lock</span>
                  <input
                    id="password"
                    type={showPassword ? 'text' : 'password'}
                    name="password"
                    value={formData.password}
                    onChange={handleChange}
                    placeholder="••••••••"
                    className="w-full pl-12 pr-12 py-4 bg-surface-container-low border-none rounded-xl focus:ring-2 focus:ring-primary/20 focus:bg-surface-container-lowest transition-all duration-200 text-on-surface placeholder:text-outline"
                    disabled={registerMutation.isPending}
                  />
                  <button
                    type="button"
                    onClick={() => setShowPassword(!showPassword)}
                    className="absolute right-4 top-1/2 -translate-y-1/2 text-outline hover:text-on-surface transition-colors"
                  >
                    <span className="material-symbols-outlined">
                      {showPassword ? 'visibility_off' : 'visibility'}
                    </span>
                  </button>
                </div>
                {errors.password && (
                  <p className="text-red-600 text-sm font-medium">{errors.password}</p>
                )}
              </div>

              {/* Confirm Password Field */}
              <div className="space-y-2">
                <label htmlFor="confirmPassword" className="block text-sm font-semibold font-label text-on-surface-variant uppercase tracking-wider">
                  Confirm Password
                </label>
                <div className="relative">
                  <span className="absolute left-4 top-1/2 -translate-y-1/2 material-symbols-outlined text-outline">lock</span>
                  <input
                    id="confirmPassword"
                    type={showConfirmPassword ? 'text' : 'password'}
                    name="confirmPassword"
                    value={formData.confirmPassword}
                    onChange={handleChange}
                    placeholder="••••••••"
                    className="w-full pl-12 pr-12 py-4 bg-surface-container-low border-none rounded-xl focus:ring-2 focus:ring-primary/20 focus:bg-surface-container-lowest transition-all duration-200 text-on-surface placeholder:text-outline"
                    disabled={registerMutation.isPending}
                  />
                  <button
                    type="button"
                    onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                    className="absolute right-4 top-1/2 -translate-y-1/2 text-outline hover:text-on-surface transition-colors"
                  >
                    <span className="material-symbols-outlined">
                      {showConfirmPassword ? 'visibility_off' : 'visibility'}
                    </span>
                  </button>
                </div>
                {errors.confirmPassword && (
                  <p className="text-red-600 text-sm font-medium">{errors.confirmPassword}</p>
                )}
              </div>

              {/* Terms Agreement */}
              <div className="flex items-center">
                <input
                  type="checkbox"
                  id="remember"
                  checked={formData.agreeToTerms}
                  onChange={handleChange}
                  name="agreeToTerms"
                  className="w-5 h-5 rounded border-outline-variant text-primary focus:ring-primary/20 transition-all cursor-pointer"
                />
                <label htmlFor="remember" className="ml-3 text-sm font-medium text-on-surface-variant cursor-pointer">
                  I agree to the{' '}
                  <a href="#" className="text-primary hover:underline">
                    Terms of Service
                  </a>{' '}
                  and{' '}
                  <a href="#" className="text-primary hover:underline">
                    Privacy Policy
                  </a>
                </label>
              </div>
              {errors.agreeToTerms && (
                <p className="text-red-600 text-sm font-medium">{errors.agreeToTerms}</p>
              )}

              {/* Submit Button */}
              <button
                type="submit"
                disabled={registerMutation.isPending}
                className="w-full py-4 px-6 bg-signature-gradient text-white font-bold font-headline rounded-xl shadow-lg shadow-primary/10 hover:shadow-primary/20 hover:scale-[0.99] active:scale-[0.97] transition-all duration-200 disabled:opacity-70 disabled:cursor-not-allowed"
              >
                {registerMutation.isPending ? 'Creating account...' : 'Create account'}
              </button>
            </form>

              {/* Login Link */}
              <p className="mt-10 text-center text-on-surface-variant font-body">
                Already have an account?{' '}
                <Link to="/auth/login" className="text-primary font-bold hover:underline ml-1">
                  Login
                </Link>
              </p>
            </div>
          </div>
        </div>
      </main>

      {/* Footer */}
      <footer className="w-full py-8 bg-surface border-t border-surface-container-low">
        <div className="flex flex-col md:flex-row justify-between items-center px-8 gap-4">
          <div className="text-on-surface-variant font-label text-xs tracking-wide uppercase">
            © 2024 InventoryApp. All rights reserved.
          </div>
          <div className="flex gap-8">
            <a href="#" className="text-on-surface-variant font-label text-xs tracking-wide uppercase hover:text-primary transition-colors opacity-80 hover:opacity-100">
              Privacy Policy
            </a>
            <a href="#" className="text-on-surface-variant font-label text-xs tracking-wide uppercase hover:text-primary transition-colors opacity-80 hover:opacity-100">
              Terms of Service
            </a>
            <a href="#" className="text-on-surface-variant font-label text-xs tracking-wide uppercase hover:text-primary transition-colors opacity-80 hover:opacity-100">
              Help Center
            </a>
          </div>
        </div>
      </footer>
    </div>
  )
}

export default RegisterPage
