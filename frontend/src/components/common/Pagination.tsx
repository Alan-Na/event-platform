import { Button } from '@/components/common/Button';
import { usePagination } from '@/hooks/usePagination';

interface PaginationProps {
  page: number;
  totalPages: number;
  onChange: (page: number) => void;
}

export function Pagination({ page, totalPages, onChange }: PaginationProps) {
  const pages = usePagination(totalPages);
  if (totalPages <= 1) return null;

  return (
    <div className="flex flex-wrap items-center gap-2">
      {pages.map((index) => (
        <Button key={index} variant={index === page ? 'primary' : 'secondary'} onClick={() => onChange(index)}>
          {index + 1}
        </Button>
      ))}
    </div>
  );
}
