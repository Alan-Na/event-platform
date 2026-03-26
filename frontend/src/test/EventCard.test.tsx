import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { EventCard } from '@/components/events/EventCard';

describe('EventCard', () => {
  it('renders title and city', () => {
    render(
      <MemoryRouter>
        <EventCard
          event={{
            id: 1,
            slug: 'spring-boot-meetup',
            title: 'Spring Boot Meetup',
            summary: 'Backend architecture and testing',
            coverImageUrl: '',
            city: 'Toronto',
            locationName: 'Tech Hub',
            startTime: '2026-04-20T18:30:00Z',
            endTime: '2026-04-20T20:30:00Z',
            registrationDeadline: '2026-04-18T18:30:00Z',
            capacity: 30,
            confirmedCount: 10,
            remainingSeats: 20,
            featured: true,
            status: 'PUBLISHED',
            categoryCode: 'TECH',
            tags: ['java', 'spring']
          }}
        />
      </MemoryRouter>
    );

    expect(screen.getByText('Spring Boot Meetup')).toBeInTheDocument();
    expect(screen.getByText(/Toronto/)).toBeInTheDocument();
  });
});
