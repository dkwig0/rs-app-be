import { Injectable } from '@nestjs/common';
import axios from 'axios';
import { Request } from 'express';
import * as dotenv from 'dotenv';
import * as NodeCache from 'node-cache';
dotenv.config();

@Injectable()
export class AppService {
  private cache: NodeCache;

  constructor() {
    this.cache = new NodeCache({ stdTTL: 120 });
  }

  async forwardRequest(req: Request) {
    const { method, originalUrl, body, headers } = req;

    const requestUrl = req.url;
    let requestUrlParts = requestUrl.split('/');
    requestUrlParts.shift();
    const recipientUrl = process.env[requestUrlParts.shift()];
    if (!recipientUrl) {
      throw { status: 502, data: { error: 'Cannot process request' } };
    }

    const hasBody = Object.keys(req.body || {}).length > 0;

    const isProductsReq = originalUrl.includes('products') && method === 'GET';

    if (isProductsReq) {
      const cachedResponse = this.cache.get(originalUrl);
      if (cachedResponse) {
        return cachedResponse;
      }
    }

    if (recipientUrl) {
      const axiosConfig = {
        headers: {
          authorization: headers.authorization,
        },
        method,
        url: `${recipientUrl}${requestUrlParts.length > 0 ? '/' + requestUrlParts.join('/') : ''}`,
        ...(hasBody && { data: body }),
      };

      try {
        const response = await axios(axiosConfig);
        if (isProductsReq) {
          this.cache.set(originalUrl, response.data, 120);
        }
        return response.data;
      } catch (error) {
        if (error.response) {
          const { status, data } = error.response;
          throw { status, data };
        } else {
          throw { status: 500, data: { error: error.message } };
        }
      }
    }
  }
}