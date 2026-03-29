import React, { useState } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import { useMutation } from '@tanstack/react-query'
import authApi from '@api/auth'
import { useAuthStore } from '@stores/authStore'
import { validatePassword } from '@utils/validators'
import { ApiError } from '@types'

interface FormErrors {
  username?: string
  password?: string
  submit?: string
}

/**
 * Login Page - Material Design Split Layout
 * Left: Warehouse image + Bento feature grid
 * Right: Modern login form with social buttons
 */
const LoginPage: React.FC = () => {
  const navigate = useNavigate()
  const { isAdmin } = useAuthStore()
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [rememberMe, setRememberMe] = useState(false)
  const [showPassword, setShowPassword] = useState(false)
  const [errors, setErrors] = useState<FormErrors>({})

  const loginMutation = useMutation({
    mutationFn: () => authApi.login({ username, password }),
    onSuccess: () => {
      setUsername('')
      setPassword('')
      setErrors({})
      if (isAdmin()) {
        navigate('/admin/dashboard')
      } else {
        navigate('/shop/products')
      }
    },
    onError: (error: any) => {
      const apiError = error.response?.data as ApiError | undefined
      const errorMessage = apiError?.message || 'Login failed. Please try again.'
      let displayMessage = errorMessage
      if (errorMessage.includes('Bad Credentials') || errorMessage.includes('Unauthorized')) {
        displayMessage = 'Invalid username or password'
      }
      setErrors({ submit: displayMessage })
    },
  })

  const validateForm = (): boolean => {
    const newErrors: FormErrors = {}
    if (!username) newErrors.username = 'Username is required'
    if (!password) newErrors.password = 'Password is required'
    setErrors(newErrors)
    return Object.keys(newErrors).length === 0
  }

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    if (validateForm()) {
      loginMutation.mutate()
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
                Precision tracking for <span className="text-primary">curated</span> collections.
              </h1>

              {/* Description */}
              <p className="text-xl text-on-surface-variant leading-relaxed font-body mb-10">
                Experience the executive dashboard designed for absolute control over your global inventory ecosystem.
              </p>

              {/* Bento Grid - 2 Feature Cards */}
              <div className="grid grid-cols-2 gap-4">
                <div className="bg-surface-container-lowest p-6 rounded-2xl shadow-sm border border-outline-variant/10">
                  <span className="material-symbols-outlined text-primary mb-3 block">analytics</span>
                  <div className="font-headline font-bold text-on-surface">Real-time Analytics</div>
                </div>
                <div className="bg-surface-container-lowest p-6 rounded-2xl shadow-sm border border-outline-variant/10">
                  <span className="material-symbols-outlined text-primary mb-3 block">hub</span>
                  <div className="font-headline font-bold text-on-surface">Multi-node Sync</div>
                </div>
              </div>
            </div>
          </div>

          {/* RIGHT: Login Form Section */}
          <div className="w-full lg:w-1/2 flex items-center justify-center p-6 sm:p-12 md:p-20 bg-surface">
            <div className="w-full max-w-md">
              {/* Form Header */}
              <div className="mb-10 text-center lg:text-left">
                <h2 className="text-3xl font-extrabold font-headline text-on-surface tracking-tight mb-2">Welcome back</h2>
                <p className="text-on-surface-variant font-body">Login to your account to manage your inventory.</p>
              </div>

              {/* Error Message */}
              {errors.submit && (
                <div className="mb-6 p-4 bg-red-50/50 border border-red-200/50 text-red-700 rounded-xl text-sm">
                  ⚠️ {errors.submit}
                </div>
              )}

              {/* Login Form */}
              <form onSubmit={handleSubmit} className="space-y-6">
                {/* Email Field */}
                <div className="space-y-2">
                  <label htmlFor="email" className="block text-sm font-semibold font-label text-on-surface-variant uppercase tracking-wider">
                    Email Address
                  </label>
                  <div className="relative">
                    <span className="absolute left-4 top-1/2 -translate-y-1/2 material-symbols-outlined text-outline">mail</span>
                    <input
                      id="email"
                      type="text"
                      value={username}
                      onChange={(e) => {
                        setUsername(e.target.value)
                        if (errors.username) setErrors({ ...errors, username: undefined })
                      }}
                      placeholder="name@company.com"
                      className="w-full pl-12 pr-4 py-4 bg-surface-container-low border-none rounded-xl focus:ring-2 focus:ring-primary/20 focus:bg-surface-container-lowest transition-all duration-200 text-on-surface placeholder:text-outline"
                      disabled={loginMutation.isPending}
                    />
                  </div>
                  {errors.username && <p className="text-red-600 text-sm font-medium mt-1">{errors.username}</p>}
                </div>

                {/* Password Field */}
                <div className="space-y-2">
                  <div className="flex justify-between items-center">
                    <label htmlFor="password" className="block text-sm font-semibold font-label text-on-surface-variant uppercase tracking-wider">
                      Password
                    </label>
                    <Link to="/auth/forgot-password" className="text-sm font-medium text-primary hover:text-blue-700 transition-colors">
                      Forgot password?
                    </Link>
                  </div>
                  <div className="relative">
                    <span className="absolute left-4 top-1/2 -translate-y-1/2 material-symbols-outlined text-outline">lock</span>
                    <input
                      id="password"
                      type={showPassword ? 'text' : 'password'}
                      value={password}
                      onChange={(e) => {
                        setPassword(e.target.value)
                        if (errors.password) setErrors({ ...errors, password: undefined })
                      }}
                      placeholder="••••••••"
                      className="w-full pl-12 pr-12 py-4 bg-surface-container-low border-none rounded-xl focus:ring-2 focus:ring-primary/20 focus:bg-surface-container-lowest transition-all duration-200 text-on-surface placeholder:text-outline"
                      disabled={loginMutation.isPending}
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
                  {errors.password && <p className="text-red-600 text-sm font-medium mt-1">{errors.password}</p>}
                </div>

                {/* Remember Me Checkbox */}
                <div className="flex items-center">
                  <input
                    type="checkbox"
                    id="remember"
                    checked={rememberMe}
                    onChange={(e) => setRememberMe(e.target.checked)}
                    className="w-5 h-5 rounded border-outline-variant text-primary focus:ring-primary/20 transition-all cursor-pointer"
                  />
                  <label htmlFor="remember" className="ml-3 text-sm font-medium text-on-surface-variant cursor-pointer">
                    Remember me for 30 days
                  </label>
                </div>

                {/* Login Button */}
                <button
                  type="submit"
                  disabled={loginMutation.isPending}
                  className="w-full py-4 px-6 bg-signature-gradient text-white font-bold font-headline rounded-xl shadow-lg shadow-primary/10 hover:shadow-primary/20 hover:scale-[0.99] active:scale-[0.97] transition-all duration-200 disabled:opacity-70 disabled:cursor-not-allowed"
                >
                  {loginMutation.isPending ? 'Signing in...' : 'Login to Dashboard'}
                </button>
              </form>

              {/* Social Divider */}
              <div className="relative my-10">
                <div className="absolute inset-0 flex items-center">
                  <div className="w-full border-t border-surface-container-high"></div>
                </div>
                <div className="relative flex justify-center text-sm">
                  <span className="px-4 bg-surface text-outline font-label uppercase tracking-widest">Or continue with</span>
                </div>
              </div>

              {/* Social Login Buttons */}
              <div className="grid grid-cols-2 gap-4">
                <button type="button" className="flex items-center justify-center gap-3 py-3 px-4 bg-surface-container-low hover:bg-surface-container-high rounded-xl transition-colors duration-200 font-medium text-on-surface">
                  <svg className="w-5 h-5" viewBox="0 0 24 24" fill="currentColor">
                    <path d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z" fill="#4285F4"/>
                    <path d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z" fill="#34A853"/>
                    <path d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z" fill="#FBBC05"/>
                    <path d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z" fill="#EA4335"/>
                  </svg>
                  <span>Google</span>
                </button>
                <button type="button" className="flex items-center justify-center gap-3 py-3 px-4 bg-surface-container-low hover:bg-surface-container-high rounded-xl transition-colors duration-200 font-medium text-on-surface">
                  <span className="material-symbols-outlined text-on-surface" style={{ fontVariationSettings: "'FILL' 1" }}>ios</span>
                  <span>Apple</span>
                </button>
              </div>

              {/* Register Link */}
              <p className="mt-10 text-center text-on-surface-variant font-body">
                Don't have an account?{' '}
                <Link to="/auth/register" className="text-primary font-bold hover:underline ml-1">
                  Register for free
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

export default LoginPage