import { useNavigate } from 'react-router-dom';
import { LoginForm } from '@/components/auth/LoginForm';
import { useAuthStore } from '@/store/auth.store';
import { useUiStore } from '@/store/ui.store';

export function LoginPage() {
  const navigate = useNavigate();
  const login = useAuthStore((state) => state.login);
  const loading = useAuthStore((state) => state.loading);
  const addToast = useUiStore((state) => state.addToast);

  return (
    <div className="container-page py-10">
      <LoginForm
        loading={loading}
        onSubmit={async (values) => {
          try {
            await login(values);
            addToast({ title: 'Login successful', tone: 'success' });
            navigate('/events');
          } catch {
            addToast({ title: 'Login failed', tone: 'error' });
          }
        }}
      />
    </div>
  );
}
