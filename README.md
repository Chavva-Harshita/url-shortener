🍊 SNIP.
SNIP. is a URL shortener that turns long URLs into short, shareable
links.

What It Does
- Converts long URLs into short links that are easier to share.
- Redirects anyone who opens a short link to the original URL.
- Tracks link clicks and provides URL statistics.
- 
Tech Stack
- Java & Spring Boot
- MySQL
- HTML, CSS & JavaScript
- Prometheus & Grafana for monitoring

Running Locally
1. Clone the repository:
   git clone https://github.com/Chavva-Harshita/url-shortener.git
   cd url-shortener
2. Create a MySQL database named url_shortner.
3. Configure your database credentials in
   src/main/resources/application.properties.
4. Start the application:
   .\mvnw.cmd spring-boot:run
The backend runs at http://localhost:8080 by default.

Monitoring
Spring Boot Actuator and Micrometer expose application metrics for
Prometheus, which can be visualized using Grafana. Prometheus and
Grafana are currently configured for local development.

Project Status
- Frontend: deployed on Vercel
- Backend deployment and hosted database: in progress
- Backend deployment and hosted database: in progress
Author
Harshita Chavva · GitHub
