import { useNavigate } from 'react-router-dom';
import { RegisterForm } from '@/components/auth/RegisterForm';
import { useAuthStore } from '@/store/auth.store';
import { useUiStore } from '@/store/ui.store';

export function RegisterPage() {
  const navigate = useNavigate();
  const register = useAuthStore((state) => state.register);
  const loading = useAuthStore((state) => state.loading);
  const addToast = useUiStore((state) => state.addToast);

  return (
    <div className="container-page py-10">
      <RegisterForm
        loading={loading}
        onSubmit={async (values) => {
          try {
            await register(values);
            addToast({ title: 'Registration successful', tone: 'success' });
            navigate('/events');
          } catch {
            addToast({ title: 'Registration failed', tone: 'error' });
          }
        }}
      />
    </div>
  );
}
