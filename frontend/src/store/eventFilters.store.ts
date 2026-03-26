import { create } from 'zustand';

interface EventFiltersState {
  keyword: string;
  category: string;
  city: string;
  startDateFrom: string;
  startDateTo: string;
  sort: string;
  page: number;
  size: number;
  setField: (key: string, value: string | number) => void;
  resetFilters: () => void;
}

const initialState = {
  keyword: '',
  category: '',
  city: '',
  startDateFrom: '',
  startDateTo: '',
  sort: 'START_ASC',
  page: 0,
  size: 9
};

export const useEventFiltersStore = create<EventFiltersState>((set) => ({
  ...initialState,
  setField(key, value) {
    set((state) => ({ ...state, [key]: value }));
  },
  resetFilters() {
    set(initialState);
  }
}));
