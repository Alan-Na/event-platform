import { Navigate, Outlet, createBrowserRouter } from 'react-router-dom';
import { MainLayout } from '@/components/layout/MainLayout';
import { AdminLayout } from '@/components/layout/AdminLayout';
import { HomePage } from '@/pages/HomePage';
import { EventsPage } from '@/pages/EventsPage';
import { EventDetailPage } from '@/pages/EventDetailPage';
import { LoginPage } from '@/pages/LoginPage';
import { RegisterPage } from '@/pages/RegisterPage';
import { MyBookingsPage } from '@/pages/MyBookingsPage';
import { ProfilePage } from '@/pages/ProfilePage';
import { AdminDashboardPage } from '@/pages/admin/AdminDashboardPage';
import { AdminEventsPage } from '@/pages/admin/AdminEventsPage';
import { AdminEventCreatePage } from '@/pages/admin/AdminEventCreatePage';
import { AdminEventEditPage } from '@/pages/admin/AdminEventEditPage';
import { AdminRegistrationsPage } from '@/pages/admin/AdminRegistrationsPage';
import { AdminUsersPage } from '@/pages/admin/AdminUsersPage';
import { useAuthStore } from '@/store/auth.store';
import { isAdmin } from '@/utils/guards';

function ProtectedRoute() {
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated);
  return isAuthenticated ? <Outlet /> : <Navigate to="/login" replace />;
}

function AdminRoute() {
  const user = useAuthStore((state) => state.user);
  return isAdmin(user) ? <Outlet /> : <Navigate to="/" replace />;
}

function PublicOnlyRoute() {
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated);
  return !isAuthenticated ? <Outlet /> : <Navigate to="/events" replace />;
}

export const router = createBrowserRouter([
  {
    path: '/',
    element: <MainLayout />,
    children: [
      { index: true, element: <HomePage /> },
      { path: 'events', element: <EventsPage /> },
      { path: 'events/:id', element: <EventDetailPage /> },
      {
        element: <PublicOnlyRoute />,
        children: [
          { path: 'login', element: <LoginPage /> },
          { path: 'register', element: <RegisterPage /> }
        ]
      },
      {
        element: <ProtectedRoute />,
        children: [
          { path: 'bookings', element: <MyBookingsPage /> },
          { path: 'profile', element: <ProfilePage /> }
        ]
      }
    ]
  },
  {
    path: '/admin',
    element: <AdminLayout />,
    children: [
      {
        element: <AdminRoute />,
        children: [
          { index: true, element: <AdminDashboardPage /> },
          { path: 'events', element: <AdminEventsPage /> },
          { path: 'events/new', element: <AdminEventCreatePage /> },
          { path: 'events/:id/edit', element: <AdminEventEditPage /> },
          { path: 'events/:id/registrations', element: <AdminRegistrationsPage /> },
          { path: 'users', element: <AdminUsersPage /> }
        ]
      }
    ]
  }
]);
