import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { LoginForm } from '@/components/auth/LoginForm';

describe('LoginForm', () => {
  it('submits email and password', async () => {
    const user = userEvent.setup();
    const handleSubmit = vi.fn().mockResolvedValue(undefined);

    render(<LoginForm onSubmit={handleSubmit} />);

    await user.type(screen.getByLabelText(/Email/i), 'alice@example.com');
    await user.type(screen.getByLabelText(/Password/i), 'User123!');
    await user.click(screen.getByRole('button', { name: /Sign in/i }));

    expect(handleSubmit).toHaveBeenCalledWith({ email: 'alice@example.com', password: 'User123!' });
  });
});
