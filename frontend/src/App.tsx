import React, { useEffect } from 'react'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { BrowserRouter as Router } from 'react-router-dom'
import { restoreAuthSession } from '@stores/authStore'
import Routes from '@routes/index'

// Create React Query client
const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 1000 * 60, // 1 minute
      gcTime: 1000 * 60 * 5, // 5 minutes
      retry: 1,
    },
  },
})

function App() {
  useEffect(() => {
    // Restore auth session on app load
    restoreAuthSession()
  }, [])

  return (
    <QueryClientProvider client={queryClient}>
      <Router>
        <Routes />
      </Router>
    </QueryClientProvider>
  )
}

export default App
