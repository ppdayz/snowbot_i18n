package com.csjbot.snowbot.bean.aiui;

import java.math.BigDecimal;
import java.util.Stack;

public class Compute {
	Stack bdStack = new Stack();// 存放BigDecimal对象的栈
	Stack opStack = new Stack();// 存放运算符或括号的栈
	boolean error = false;

	public String compute(String exp) {
		int len = exp.length();
		int i = 0;
		String digitStr = "";
		BigDecimal result = new BigDecimal("0");
		opStack.push('#');
		// System.out.println(exp);
		end: for (i = 0; i < len; i++) {
			char temp = exp.charAt(i);
			if (isDigit(temp)) {
				while ((isDigit(temp) || temp == '.') && i < len) {
					i++;
					digitStr = digitStr + temp;
					temp = exp.charAt(i);
				}
				// System.out.println("digitString=: "+digitStr);
				bdStack.push(new BigDecimal(digitStr));
				digitStr = "";
				i--;// 避免跳过非数字字符
			}
			// 如果表达式的加,减号表示了数字的符号,则认为不是运算符
			else if ((i == 0 && (temp == '+' || temp == '-'))
					|| (i >= 1 && exp.charAt(i - 1) == '(' && (temp == '+' || temp == '-'))) { // 在存放BigDecimal对象的栈入栈new
																								// BigDecimal("0");
				bdStack.push(new BigDecimal("0"));
				// 将加,减号入栈opStack使之为运算符
				opStack.push(temp);
			} else {
				switch (((Character) opStack.peek()).charValue()) {
				case '#': {
					if (temp == '=') {
						result = (BigDecimal) bdStack.pop();
					} else {
						opStack.push(temp);
					}

				}
					break;
				case '+': {
					if (temp == '+' || temp == '-' || temp == ')'
							|| temp == '=') {
						BigDecimal b = (BigDecimal) bdStack.pop();
						BigDecimal a = (BigDecimal) bdStack.pop();
						bdStack.push(a.add(b));
						opStack.pop();
						i--;
					} else {
						opStack.push(temp);
					}
				}
					break;
				case '-': {
					if (temp == '+' || temp == '-' || temp == ')'
							|| temp == '=') {
						BigDecimal b = (BigDecimal) bdStack.pop();
						BigDecimal a = (BigDecimal) bdStack.pop();
						bdStack.push(a.subtract(b));
						opStack.pop();
						i--;
					} else {
						opStack.push(temp);
					}
				}
					break;
				case '*': {
					if (temp == '(') {
						opStack.push(temp);
					} else {
						BigDecimal b = (BigDecimal) bdStack.pop();
						BigDecimal a = (BigDecimal) bdStack.pop();
						bdStack.push(a.multiply(b));
						opStack.pop();
						i--;
					}
				}
					break;
				case '/': {
					if (temp == '(') {
						opStack.push(temp);
					} else {
						BigDecimal b = (BigDecimal) bdStack.pop();
						BigDecimal a = (BigDecimal) bdStack.pop();
						if (b.abs().doubleValue() == 0.0) {
							error = true;
							break end;
						} else {
							bdStack.push(a
									.divide(b, 100, BigDecimal.ROUND_DOWN));
							opStack.pop();
							i--;
						}
					}
				}
					break;
				case '(': {
					if (temp != ')') {
						opStack.push(temp);
					} else {
						opStack.pop();
					}
				}
					break;
				}
			}
		}
		if (error == true) {
			return "Error:出现了除数为零的情况!";
		} else {
			return (result.setScale(3, BigDecimal.ROUND_HALF_DOWN)).toString();
		}
	}

	public boolean isDigit(char ch) {
		return ch >= '0' && ch <= '9';
	}
}