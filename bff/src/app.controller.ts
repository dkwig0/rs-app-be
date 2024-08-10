import { All, Controller, Req, Res } from '@nestjs/common';
import { AppService } from './app.service';
import { Request, Response } from 'express';

@Controller()
export class AppController {
  constructor(private readonly appService: AppService) {}

  @All('/*')
  async handleFERequest(@Req() req: Request, @Res() res: Response) {
    try {
      const data = await this.appService.forwardRequest(req);
      res.json(data);
    } catch (error) {
      if (error.status) {
        res.status(error.status).json(error.data);
      } else {
        res.status(500).json({ error: error.message });
      }
    }
  }
}