import React from 'react'
import { Outlet } from 'react-router-dom'

/**
 * Public Layout
 * Used for login, register pages (not authenticated)
 */
const PublicLayout: React.FC = () => {
  return (
    <div className="min-h-screen bg-gray-100 flex flex-col">
      {/* Header */}
      <header className="bg-white shadow-sm py-4">
        <div className="container mx-auto px-4">
          <h1 className="text-2xl font-bold text-blue-600">E-Commerce System</h1>
        </div>
      </header>

      {/* Main Content */}
      <main className="flex-1 container mx-auto px-4 py-8">
        <Outlet />
      </main>

      {/* Footer */}
      <footer className="bg-white border-t border-gray-200 py-4 mt-8">
        <div className="container mx-auto px-4 text-center text-gray-600">
          <p>&copy; 2026 E-Commerce Inventory System. All rights reserved.</p>
        </div>
      </footer>
    </div>
  )
}

export default PublicLayout
