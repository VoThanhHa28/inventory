import React, { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useDashboardStats, useRecentActivity } from '@api/dashboard'
import { useAuthStore } from '@stores/authStore'

/**
 * DashboardPage - Premium Inventory Dashboard
 * Full-featured dashboard with sidebar, header, KPI metrics, product recommendations, and analytics
 */
const DashboardPage: React.FC = () => {
  const navigate = useNavigate()
  const { user, logout } = useAuthStore()
  const [isMobileNavOpen, setIsMobileNavOpen] = useState(false)

  const { data: stats, isLoading: statsLoading, error: statsError } = useDashboardStats()
  const { data: activities, isLoading: activitiesLoading } = useRecentActivity(10)

  const formatCurrency = (value: number | undefined): string => {
    if (!value) return '$0'
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD',
      minimumFractionDigits: 0,
      maximumFractionDigits: 0,
    }).format(value)
  }

  const formatEfficiency = (value: number | undefined): string => {
    if (!value) return '0%'
    return `${Math.round(value)}%`
  }

  const getActivityColor = (action: string) => {
    switch (action) {
      case 'CREATE':
        return 'bg-emerald-50 text-emerald-700'
      case 'UPDATE':
        return 'bg-blue-50 text-blue-700'
      case 'DELETE':
        return 'bg-red-50 text-red-700'
      default:
        return 'bg-slate-50 text-slate-700'
    }
  }

  const getActivityDot = (action: string) => {
    switch (action) {
      case 'CREATE':
        return 'bg-emerald-500'
      case 'UPDATE':
        return 'bg-blue-500'
      case 'DELETE':
        return 'bg-red-500'
      default:
        return 'bg-slate-500'
    }
  }

  const handleLogout = () => {
    logout()
    navigate('/auth/login')
  }

  // Sample product recommendations
  const productRecommendations = [
    {
      id: 1,
      name: 'Precision Chrono X1',
      price: 899,
      description: 'High-performance timekeeping with titanium casing and water resistance up to 100m.',
      badge: 'Premium',
      badgeColor: 'bg-gray-900 text-white',
      image: 'https://via.placeholder.com/300x250/333/fff?text=Premium+Watch',
    },
    {
      id: 2,
      name: 'Sonic Bloom Pro',
      price: 349,
      description: 'Professional studio headphones with adaptive noise cancellation and 40-hour battery life.',
      badge: 'Best Seller',
      badgeColor: 'bg-blue-600 text-white',
      image: 'https://via.placeholder.com/300x250/2563eb/fff?text=Headphones',
    },
    {
      id: 3,
      name: 'Classic Frame IV',
      price: 210,
      description: 'Hand-crafted acetate frames with polarized lenses, blending timeless style with modern tech.',
      badge: 'Limited Edition',
      badgeColor: 'bg-slate-500 text-white',
      image: 'https://via.placeholder.com/300x250/64748b/fff?text=Eyewear',
    },
  ]

  if (statsLoading) {
    return (
      <div className="min-h-screen bg-slate-50 flex items-center justify-center">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto mb-4"></div>
          <p className="text-slate-600 font-medium">Loading dashboard...</p>
        </div>
      </div>
    )
  }

  if (statsError) {
    return (
      <div className="min-h-screen bg-slate-50 flex items-center justify-center">
        <div className="text-center">
          <p className="text-red-600 font-medium mb-2">Error loading dashboard</p>
          <p className="text-slate-600 text-sm">Please try refreshing the page</p>
          <button
            onClick={() => window.location.reload()}
            className="mt-4 px-4 py-2 bg-blue-600 text-white rounded-lg font-medium text-sm hover:bg-blue-700"
          >
            Retry
          </button>
        </div>
      </div>
    )
  }

  return (
    <div className="flex h-screen bg-slate-50 overflow-hidden">
      {/* ============ SIDEBAR ============ */}
      <aside className="w-64 fixed left-0 top-0 h-screen bg-gradient-to-b from-slate-50 to-slate-100 z-50 flex flex-col py-8 shadow-sm border-r border-slate-200 hidden md:flex">
        {/* Logo Section */}
        <div className="px-8 mb-12">
          <h1 className="text-2xl font-bold tracking-tight text-blue-600">InventoryApp</h1>
          <p className="text-xs font-medium tracking-wider uppercase text-slate-400 mt-1">Premium Curator</p>
        </div>

        {/* Navigation */}
        <nav className="flex-1 space-y-1 px-6">
          {/* Active: Dashboard */}
          <button
            onClick={() => navigate('/admin/dashboard')}
            className="w-full flex items-center space-x-3 px-6 py-4 bg-blue-50 text-blue-700 font-semibold border-r-4 border-blue-600 rounded-lg transition-colors"
          >
            <span className="text-xl">📊</span>
            <span className="text-sm tracking-wider uppercase font-medium">Dashboard</span>
          </button>

          {/* Inventory */}
          <button
            onClick={() => navigate('/admin/inventory')}
            className="w-full flex items-center space-x-3 px-6 py-4 text-slate-500 hover:text-blue-600 hover:bg-slate-200/50 rounded-lg transition-colors"
          >
            <span className="text-xl">📦</span>
            <span className="text-sm tracking-wider uppercase font-medium">Inventory</span>
          </button>

          {/* View Store */}
          <button
            onClick={() => navigate('/shop/products')}
            className="w-full flex items-center space-x-3 px-6 py-4 text-slate-500 hover:text-blue-600 hover:bg-slate-200/50 rounded-lg transition-colors"
          >
            <span className="text-xl">🛍️</span>
            <span className="text-sm tracking-wider uppercase font-medium">View Store</span>
          </button>
        </nav>

        {/* Logout */}
        <div className="px-6 mt-auto">
          <button
            onClick={handleLogout}
            className="w-full flex items-center space-x-3 px-6 py-4 text-slate-500 hover:text-red-600 hover:bg-red-50/50 rounded-lg transition-colors"
          >
            <span className="text-xl">🚪</span>
            <span className="text-sm tracking-wider uppercase font-medium">Logout</span>
          </button>
        </div>
      </aside>

      {/* ============ MAIN CONTENT ============ */}
      <main className="flex-1 md:ml-64 flex flex-col overflow-y-auto">
        {/* ============ HEADER ============ */}
        <header className="sticky top-0 z-40 w-full bg-white/70 backdrop-blur-md flex justify-between items-center h-20 px-8 md:px-12 shadow-sm border-b border-slate-200/50">
          {/* Left: Search */}
          <div className="flex items-center flex-1">
            <div className="relative w-96 hidden md:block">
              <span className="absolute inset-y-0 left-0 pl-3 flex items-center text-slate-400">🔍</span>
              <input
                type="text"
                placeholder="Search inventory..."
                className="w-full pl-10 pr-4 py-2 border border-slate-300 bg-slate-50 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:bg-white transition-all"
              />
            </div>
          </div>

          {/* Right: Icons & Profile */}
          <div className="flex items-center space-x-6 ml-8">
            <button className="relative text-slate-500 hover:text-blue-600 transition-colors">
              <span className="text-xl">🔔</span>
              <span className="absolute top-0 right-0 block h-2 w-2 rounded-full bg-red-500 ring-2 ring-white"></span>
            </button>
            <button className="text-slate-500 hover:text-blue-600 transition-colors">
              <span className="text-xl">⚙️</span>
            </button>

            {/* Divider */}
            <div className="h-6 w-px bg-slate-300"></div>

            {/* User Profile */}
            <div className="flex items-center space-x-3">
              <div className="text-right hidden sm:block">
                <p className="text-sm font-semibold text-slate-900">{user?.fullName || 'Admin'}</p>
                <p className="text-xs text-slate-500">Administrator</p>
              </div>
              <div className="h-10 w-10 rounded-full bg-gradient-to-br from-blue-400 to-blue-600 flex items-center justify-center text-white font-bold text-sm">
                {user?.fullName?.[0] || 'A'}
              </div>
            </div>
          </div>
        </header>

        {/* ============ CONTENT AREA ============ */}
        <div className="flex-1 overflow-y-auto px-12 py-12 space-y-12">
          {/* Welcome Section */}
          <section>
            <h2 className="text-3xl font-bold text-slate-900 tracking-tight mb-1">Welcome back, {user?.fullName?.split(' ')[0] || 'Admin'}!</h2>
            <p className="text-base text-slate-600 font-medium">
              Your inventory ecosystem is performing at{' '}
              <span className="text-blue-600 font-bold">{formatEfficiency(stats?.efficiencyPercentage)}</span> efficiency today.
            </p>
          </section>

          {/* KPI Cards */}
          <section className="grid grid-cols-1 md:grid-cols-3 gap-8">
            {/* KPI 1: Total Products */}
            <div className="bg-white rounded-xl p-8 shadow-sm hover:shadow-xl hover:shadow-blue-500/5 transition-all group">
              <div className="flex justify-between items-start mb-6">
                <div className="p-3 bg-blue-50 rounded-lg text-blue-600 group-hover:bg-blue-600 group-hover:text-white transition-colors">
                  <span className="text-2xl">📦</span>
                </div>
                <span className="text-sm font-semibold text-emerald-600 bg-emerald-50 px-3 py-1 rounded-md">+12%</span>
              </div>
              <p className="text-sm font-medium text-slate-500 uppercase tracking-wider mb-1">Total Products</p>
              <h3 className="text-4xl font-bold text-slate-900">{stats?.totalProducts || 0}</h3>
              <p className="text-xs text-slate-500 mt-4">Compared to last month (112)</p>
            </div>

            {/* KPI 2: Low Stock Alerts */}
            <div className="bg-white rounded-xl p-8 shadow-sm hover:shadow-xl hover:shadow-red-500/5 transition-all group">
              <div className="flex justify-between items-start mb-6">
                <div className="p-3 bg-red-50 rounded-lg text-red-600 group-hover:bg-red-600 group-hover:text-white transition-colors">
                  <span className="text-2xl">⚠️</span>
                </div>
                <div className="flex items-center space-x-2">
                  <span className="h-2 w-2 rounded-full bg-red-600 animate-pulse"></span>
                  <span className="text-xs font-bold text-red-600 uppercase">
                    {(stats?.lowStockCount || 0) > 0 ? 'Critical' : 'Healthy'}
                  </span>
                </div>
              </div>
              <p className="text-sm font-medium text-slate-500 uppercase tracking-wider mb-1">Low Stock Alerts</p>
              <h3 className="text-4xl font-bold text-slate-900">{stats?.lowStockCount || 0}</h3>
              <p className={`text-xs font-medium mt-4 ${(stats?.lowStockCount || 0) > 0 ? 'text-red-600' : 'text-emerald-600'}`}>
                {(stats?.lowStockCount || 0) > 0 ? 'Urgent restock needed immediately' : 'All inventory levels healthy'}
              </p>
            </div>

            {/* KPI 3: Total Sales */}
            <div className="bg-white rounded-xl p-8 shadow-sm hover:shadow-xl hover:shadow-purple-500/5 transition-all group">
              <div className="flex justify-between items-start mb-6">
                <div className="p-3 bg-purple-50 rounded-lg text-purple-600 group-hover:bg-purple-600 group-hover:text-white transition-colors">
                  <span className="text-2xl">💎</span>
                </div>
                <span className="text-sm font-semibold text-blue-600 bg-blue-50 px-3 py-1 rounded-md">On Track</span>
              </div>
              <p className="text-sm font-medium text-slate-500 uppercase tracking-wider mb-1">Total Sales</p>
              <h3 className="text-4xl font-bold text-slate-900">{formatCurrency(stats?.totalSales)}</h3>
              <p className="text-xs text-slate-500 mt-4">Targeting $15k Q3 Revenue Goal</p>
            </div>
          </section>

          {/* Product Recommendations */}
          <section className="space-y-6">
            <div className="flex justify-between items-end">
              <div>
                <h2 className="text-2xl font-bold tracking-tight text-slate-900">Product Recommendations</h2>
                <p className="text-sm text-slate-600">Top curated items based on current market velocity</p>
              </div>
              <a href="/shop/products" className="text-sm font-bold text-blue-600 hover:underline flex items-center gap-1">
                View Analytics →
              </a>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
              {productRecommendations.map((product) => (
                <div key={product.id} className="bg-white rounded-xl overflow-hidden shadow-sm hover:shadow-2xl transition-all flex flex-col">
                  {/* Product Image */}
                  <div className="relative h-64 overflow-hidden bg-gradient-to-br from-slate-200 to-slate-300 flex items-center justify-center">
                    <img
                      src={product.image}
                      alt={product.name}
                      className="w-full h-full object-cover transition-transform duration-700 hover:scale-110"
                    />
                    <div className="absolute top-4 left-4">
                      <span className={`${product.badgeColor} text-[10px] font-bold px-3 py-1 rounded-full uppercase tracking-widest`}>
                        {product.badge}
                      </span>
                    </div>
                  </div>

                  {/* Product Details */}
                  <div className="p-6 flex-1 flex flex-col">
                    <div className="mb-4">
                      <h4 className="text-xl font-bold text-slate-900 mb-1">{product.name}</h4>
                      <p className="text-sm text-slate-600 line-clamp-2">{product.description}</p>
                    </div>

                    <div className="mt-auto flex items-center justify-between">
                      <span className="text-2xl font-black text-slate-900">${product.price}</span>
                      <button className="bg-gradient-to-br from-blue-600 to-blue-700 text-white px-4 py-2 rounded-lg text-sm font-bold flex items-center gap-2 hover:shadow-lg transition-all active:scale-95">
                        <span>🛒</span>
                        <span>Add</span>
                      </button>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </section>

          {/* Bottom Section: Geographic Reach + Activity */}
          <section className="grid grid-cols-1 lg:grid-cols-5 gap-8">
            {/* Left: Geographic Reach */}
            <div className="lg:col-span-3 bg-slate-100/50 rounded-2xl p-8 flex flex-col justify-between">
              <div>
                <h3 className="text-2xl font-bold text-slate-900 mb-2">Inventory Geographic Reach</h3>
                <p className="text-slate-600 mb-8">Real-time distribution of stock across regional hubs.</p>

                {/* Map Placeholder */}
                <div className="h-64 bg-white rounded-xl flex items-center justify-center border-2 border-dashed border-slate-300 overflow-hidden relative">
                  <div className="relative z-10 flex flex-col items-center">
                    <span className="text-4xl mb-2">🌍</span>
                    <p className="text-sm font-semibold uppercase tracking-widest text-slate-600">Network Online</p>
                  </div>
                </div>
              </div>

              {/* Regional Stats */}
              <div className="mt-8 flex space-x-12 pt-8 border-t border-slate-300">
                <div>
                  <p className="text-xs font-medium uppercase tracking-wider text-slate-600 mb-1">North America</p>
                  <p className="text-2xl font-bold text-slate-900">62%</p>
                </div>
                <div>
                  <p className="text-xs font-medium uppercase tracking-wider text-slate-600 mb-1">Europe</p>
                  <p className="text-2xl font-bold text-slate-900">24%</p>
                </div>
                <div>
                  <p className="text-xs font-medium uppercase tracking-wider text-slate-600 mb-1">APAC</p>
                  <p className="text-2xl font-bold text-slate-900">14%</p>
                </div>
              </div>
            </div>

            {/* Right: AI Optimization + Activity */}
            <div className="lg:col-span-2 space-y-8">
              {/* AI Optimization Card */}
              <div className="bg-gradient-to-br from-blue-600 to-blue-700 text-white rounded-2xl p-8 relative overflow-hidden group">
                <div className="absolute -right-8 -bottom-8 opacity-10 group-hover:scale-110 transition-transform">
                  <span className="text-8xl">⚡</span>
                </div>
                <h3 className="text-xl font-bold mb-2 relative z-10">Optimize Your Flow</h3>
                <p className="text-blue-100 text-sm mb-6 leading-relaxed relative z-10">
                  Our AI detected that reordering bestsellers 3 days earlier could save $1,200 in expedited shipping.
                </p>
                <button className="bg-white text-blue-600 px-6 py-2.5 rounded-lg text-sm font-bold hover:bg-opacity-90 transition-colors relative z-10">
                  Run AI Simulation
                </button>
              </div>

              {/* Recent Activity */}
              <div className="bg-white rounded-2xl p-8 shadow-sm">
                <h3 className="text-lg font-bold text-slate-900 mb-6">Recent Activity</h3>
                <div className="space-y-4 max-h-96 overflow-y-auto">
                  {activitiesLoading ? (
                    <p className="text-sm text-slate-500">Loading activities...</p>
                  ) : activities && activities.length > 0 ? (
                    activities.map((activity, idx) => (
                      <div key={idx} className="flex items-start gap-3 pb-4 border-b border-slate-200 last:border-0">
                        <div className={`h-2 w-2 rounded-full mt-2 flex-shrink-0 ${getActivityDot(activity.action)}`}></div>
                        <div className="flex-1 min-w-0">
                          <p className="text-sm text-slate-900 font-medium">
                            <span className={`inline-block ${getActivityColor(activity.action)} px-2 py-0.5 rounded text-xs font-semibold mr-2`}>
                              {activity.action}
                            </span>
                            {activity.productName}
                          </p>
                          <p className="text-xs text-slate-500 mt-1">
                            by {activity.changedBy} • {new Date(activity.changedAt).toLocaleDateString()}
                          </p>
                        </div>
                      </div>
                    ))
                  ) : (
                    <p className="text-sm text-slate-500">No activities yet</p>
                  )}
                </div>
              </div>
            </div>
          </section>
        </div>

        {/* Footer */}
        <footer className="mt-auto p-12 text-center text-xs font-medium uppercase tracking-widest text-slate-400">
          InventoryApp © 2024 • Architectural Logistics System • Version 4.2.0
        </footer>
      </main>
    </div>
  )
}

export default DashboardPage
