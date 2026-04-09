import UIKit

struct OpenAIService {
    private let apiKey: String
    private let model = "gpt-4o"
    private let endpoint = URL(string: "https://api.openai.com/v1/chat/completions")!

    init(apiKey: String) {
        self.apiKey = apiKey
    }

    func getChipCount(image: UIImage, chipHints: String?) async throws -> String {
        let base64 = try bitmapToBase64(image)
        let body = buildRequestBody(base64: base64, chipHints: chipHints)
        let data = try JSONSerialization.data(withJSONObject: body)

        var request = URLRequest(url: endpoint)
        request.httpMethod = "POST"
        request.setValue("Bearer \(apiKey)", forHTTPHeaderField: "Authorization")
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        request.httpBody = data

        let (responseData, _) = try await URLSession.shared.data(for: request)
        let json = try JSONSerialization.jsonObject(with: responseData) as? [String: Any]
        let choices = json?["choices"] as? [[String: Any]]
        let message = choices?.first?["message"] as? [String: Any]
        let content = message?["content"] as? String
        return content ?? "No response"
    }

    // MARK: – Image encoding

    private func bitmapToBase64(_ image: UIImage) throws -> String {
        let maxDim: CGFloat = 1920
        let scaled: UIImage
        let size = image.size
        if max(size.width, size.height) > maxDim {
            let ratio = maxDim / max(size.width, size.height)
            let newSize = CGSize(width: size.width * ratio, height: size.height * ratio)
            UIGraphicsBeginImageContextWithOptions(newSize, false, 1.0)
            image.draw(in: CGRect(origin: .zero, size: newSize))
            scaled = UIGraphicsGetImageFromCurrentImageContext() ?? image
            UIGraphicsEndImageContext()
        } else {
            scaled = image
        }
        guard let data = scaled.jpegData(compressionQuality: 0.95) else {
            throw NSError(domain: "StaxError", code: 1, userInfo: [NSLocalizedDescriptionKey: "Could not encode image"])
        }
        return data.base64EncodedString()
    }

    // MARK: – Request body

    private func buildRequestBody(base64: String, chipHints: String?) -> [String: Any] {
        let systemPrompt = "You are an expert poker chip counter. When shown a photo of poker chips, you count the chips accurately and calculate the total value."

        var chipSection = ""
        if let hints = chipHints, !hints.isEmpty {
            chipSection = """

            The user has configured the following chip denominations (color → value):
            \(hints)

            Use these configured values when calculating totals. If you see a chip color not listed, note it separately.
            """
        }

        let userPrompt = """
        Please count the poker chips in this image and calculate the total value.

        Follow these steps:
        1. **Identify each chip denomination**: Read any numbers printed on the chip faces, or use the colors if denominations are not visible.
        2. **Count each stack**: A full stack is typically 20 chips. Count partial stacks proportionally.
        3. **Calculate subtotals**: Multiply the count of each denomination by its value.
        4. **Sum everything**: Add all subtotals for the grand total.
        \(chipSection)

        Respond with:
        - A breakdown by chip type (e.g. "25 green chips × $5 = $125")
        - The grand total (e.g. "Total: $1,250")

        Be precise. If you cannot read denominations, estimate using standard casino chip colors.
        """

        return [
            "model": model,
            "max_tokens": 600,
            "messages": [
                ["role": "system", "content": systemPrompt],
                [
                    "role": "user",
                    "content": [
                        ["type": "text", "text": userPrompt],
                        [
                            "type": "image_url",
                            "image_url": [
                                "url": "data:image/jpeg;base64,\(base64)",
                                "detail": "high"
                            ]
                        ]
                    ]
                ]
            ]
        ]
    }
}
