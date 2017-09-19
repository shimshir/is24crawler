import axios from 'axios'

class HttpClient {

    host;

    constructor(host) {
        this.host = host;
    }

    get(path) {
        return axios.get(this.host + path);
    }

    post(path, data) {
        return axios.post(this.host + path, data);
    }
}

const host = process.env.NODE_ENV === 'development' ? 'http://localhost:4242' : '';
const defaultHttpClient = new HttpClient(host);
export default defaultHttpClient
