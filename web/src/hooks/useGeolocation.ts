import { useState, useEffect } from 'react';

interface GeolocationState {
  latitude: number | null;
  longitude: number | null;
  error: string | null;
  isLoading: boolean;
}

export function useGeolocation(requestOnMount = false): GeolocationState & { request: () => void } {
  const [state, setState] = useState<GeolocationState>({
    latitude: null,
    longitude: null,
    error: null,
    isLoading: false,
  });

  function request() {
    if (!navigator.geolocation) {
      setState((s) => ({ ...s, error: 'Geolocation is not supported by your browser.' }));
      return;
    }
    setState((s) => ({ ...s, isLoading: true, error: null }));
    navigator.geolocation.getCurrentPosition(
      (pos) => {
        setState({
          latitude: pos.coords.latitude,
          longitude: pos.coords.longitude,
          error: null,
          isLoading: false,
        });
      },
      (err) => {
        setState((s) => ({ ...s, error: err.message, isLoading: false }));
      },
    );
  }

  useEffect(() => {
    if (requestOnMount) request();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  return { ...state, request };
}
