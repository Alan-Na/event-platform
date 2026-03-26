export function usePagination(totalPages: number) {
  return Array.from({ length: totalPages }, (_, index) => index);
}
