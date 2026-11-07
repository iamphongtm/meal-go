# VietMap configuration

Reverse geocoding is available at
`GET /api/search/locations/reverse?latitude=10.759221&longitude=106.675901`.
The controller calls `LocationService`, which validates coordinates and calls
`VietMapClient`. The client uses VietMap Reverse v4 with `display_type=1`.
Setting configuration alone does not verify the key or its permissions.

The response contains `latitude`, `longitude`, `name`, `ward`, `city`, and
`formattedAddress`. Coordinates retain the original request location. Optional
fields without provider data are null; house number and street are not guessed.
No address is saved. The customer must confirm the suggested address.

## Docker Compose

Copy `search-service/.env.example` to `search-service/.env`
without overwriting an existing file. Set `VIETMAP_API_KEY` locally to a key with
Reverse Geocoding access. Check quota and IP restrictions in the VietMap account.
Never put the key in chat, Git, frontend `VITE_*` variables or screenshots.

Compose loads this optional file into the search-service container. It requires
Compose 2.24+ for optional `env_file`. Changing the file does not alter an existing
container until it is recreated; no stack restart is performed by this setup.

## IDE / Maven

Spring Boot does not automatically load `.env`. Configure these environment
variables in the IDE run configuration or in the shell launching the application:

| Variable | Default | Purpose |
| --- | --- | --- |
| `VIETMAP_API_KEY` | empty | Backend-only VietMap credential |
| `VIETMAP_BASE_URL` | `https://maps.vietmap.vn` | Provider origin |
| `VIETMAP_CONNECT_TIMEOUT` | `3s` | Connection timeout |
| `VIETMAP_READ_TIMEOUT` | `5s` | Overall HTTP request timeout |

These map to `vietmap.*` in `application.yaml` and are read by `VietMapClient`.
Existing restaurant search remains usable without a VietMap key.

Do not log the API key, provider URLs containing credentials, precise user GPS
coordinates or resolved personal addresses. The provider endpoint and response
mapping follow https://maps.vietmap.vn/docs/map-api/reverse-version/reverse-v4/.
Errors use the global ProblemDetail handler with English messages. A missing key
returns 503, no result returns 404, request timeout returns 504, and invalid provider
responses return 502. Provider authentication errors are not client login errors.
