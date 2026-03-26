import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { LanguageSwitcher } from '@/components/layout/LanguageSwitcher';
import i18n from '@/i18n';

describe('LanguageSwitcher', () => {
  it('toggles language', async () => {
    const user = userEvent.setup();
    await i18n.changeLanguage('en');
    render(<LanguageSwitcher />);

    await user.click(screen.getByRole('button', { name: /Language/i }));

    expect(i18n.language).toBe('zh-CN');
  });
});
