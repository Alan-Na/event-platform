import { Outlet } from 'react-router-dom';
import { Navbar } from '@/components/layout/Navbar';
import { OrganizerSidebar } from '@/components/layout/OrganizerSidebar';

export function OrganizerLayout() {
  return (
    <div className="min-h-screen bg-slate-50">
      <Navbar />
      <div className="container-page grid gap-6 py-8 lg:grid-cols-[260px_1fr]">
        <OrganizerSidebar />
        <div className="min-w-0">
          <Outlet />
        </div>
      </div>
    </div>
  );
}
