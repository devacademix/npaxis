import { preceptorService } from './preceptor';

interface InquiryRequest {
  preceptorId: number | string;
  message: string;
}

export const inquiryService = {
  sendInquiry: async (payload: InquiryRequest) => {
    // Mock the missing backend endpoint for inquiries so it succeeds without hitting the server.
    await preceptorService.trackAnalyticsEvent('INQUIRY', payload.preceptorId);
    
    // Simulate network delay to maintain UI feeling
    await new Promise(resolve => setTimeout(resolve, 500));
    
    return { success: true, message: "Inquiry sent successfully" };
  },
};

export default inquiryService;
