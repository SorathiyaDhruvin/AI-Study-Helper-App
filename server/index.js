const express = require('express');
const Razorpay = require('razorpay');
const crypto = require('crypto');
const cors = require('cors');

const app = express();
app.use(express.json());
app.use(cors());

// IMPORTANT: Replace these with your LIVE Razorpay credentials
const RAZORPAY_KEY_ID = 'rzp_test_SboUHD0XeLg3OY'; // User: Update this to LIVE KEY ID
const RAZORPAY_KEY_SECRET = '92YZBW43LM41veraPLzhMQVL'; // Provided Secret

const razorpay = new Razorpay({
    key_id: RAZORPAY_KEY_ID,
    key_secret: RAZORPAY_KEY_SECRET,
});

// Endpoint to create an order
app.post('/create-order', async (req, res) => {
    const { amount, currency } = req.body;

    const options = {
        amount: amount, // amount in paise
        currency: currency || "INR",
        receipt: `receipt_${Date.now()}`,
    };

    try {
        const order = await razorpay.orders.create(options);
        res.status(200).json({
            success: true,
            order_id: order.id,
            amount: order.amount,
            currency: order.currency
        });
    } catch (error) {
        console.error("Order Creation Error:", error);
        res.status(500).json({ success: false, message: error.message });
    }
});

// Endpoint to verify payment signature
app.post('/verify-payment', (req, res) => {
    const { razorpay_order_id, razorpay_payment_id, razorpay_signature } = req.body;

    const sign = razorpay_order_id + "|" + razorpay_payment_id;
    const expectedSign = crypto
        .createHmac("sha256", RAZORPAY_KEY_SECRET)
        .update(sign.toString())
        .digest("hex");

    if (razorpay_signature === expectedSign) {
        res.status(200).json({ success: true, message: "Payment verified successfully" });
    } else {
        res.status(400).json({ success: false, message: "Invalid signature" });
    }
});

const PORT = process.env.PORT || 3000;
app.listen(PORT, () => {
    console.log(`Server is running on port ${PORT}`);
});
