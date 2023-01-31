# Stage 1
FROM node:16.10.0 as node
WORKDIR /app
COPY  . .
RUN npm install -g @angular/cli@14.2.10
RUN yarn install
CMD [ "ng", "serve", "--host", "0.0.0.0" ]
