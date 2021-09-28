package com.zw.platform.util;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * @Description: 简单四则运算工具类
 * @Author Tianzhangxu
 * @Date 2020/6/16 15:43
 */
public class FormulaCalculator {
    private static boolean isRightFormat = true;

    public static FormulaCalculatorResp getResult(String formula) {
        FormulaCalculatorResp resp = new FormulaCalculatorResp();
        try {
            doAnalysis(formula, resp);
        } catch (NumberFormatException nfe) {
            resp.setIsSuccess(false);
            System.out.println("公式格式有误，请检查:" + formula);
        } catch (Exception e) {
            resp.setIsSuccess(false);
            e.printStackTrace();
        }
        if (!isRightFormat) {
            resp.setIsSuccess(false);
            System.out.println("公式格式有误，请检查:" + formula);
        }
        return resp;
    }

    private static void doAnalysis(String formula, FormulaCalculatorResp resp) {
        LinkedList<Integer> stack = new LinkedList<Integer>();
        int curPos = 0;
        String beforePart = "";
        String afterPart = "";
        String calculator = "";
        isRightFormat = true;
        while (isRightFormat && (formula.indexOf('(') >= 0 || formula.indexOf(')') >= 0)) {
            curPos = 0;
            for (char s : formula.toCharArray()) {
                if (s == '(') {
                    stack.add(curPos);
                } else if (s == ')') {
                    if (stack.size() > 0) {
                        beforePart = formula.substring(0, stack.getLast());
                        afterPart = formula.substring(curPos + 1);
                        calculator = formula.substring(stack.getLast() + 1, curPos);
                        formula = beforePart + doCalculation(calculator) + afterPart;
                        stack.clear();
                        break;
                    } else {
                        System.out.println("有未关闭的右括号！");
                        isRightFormat = false;
                    }
                }
                curPos++;
            }
            if (stack.size() > 0) {
                System.out.println("有未关闭的左括号！");
                break;
            }
        }
        if (isRightFormat) {
            resp.setResultValue(doCalculation(formula));
            resp.setIsSuccess(Boolean.TRUE);
        }
    }

    private static double doCalculation(String formula) {
        ArrayList<Double> values = new ArrayList<Double>();
        ArrayList<String> operators = new ArrayList<String>();
        int curPos = 0;
        int prePos = 0;
        int minus = 0;
        for (char s : formula.toCharArray()) {
            boolean isMark = (s == '+' || s == '-' || s == '×' || s == '÷' || s == '*' || s == '/');
            if (isMark && minus != 0 && minus != 2) {
                values.add(Double.parseDouble(formula.substring(prePos, curPos).trim()));
                operators.add("" + s);
                prePos = curPos + 1;
                minus = minus + 1;
            } else {
                minus = 1;
            }
            curPos++;
        }
        values.add(Double.parseDouble(formula.substring(prePos).trim()));
        char op;
        for (curPos = 0; curPos <= operators.size() - 1; curPos++) {
            op = operators.get(curPos).charAt(0);
            switch (op) {
                case '×':
                case '*':
                    values.add(curPos, values.get(curPos) * values.get(curPos + 1));
                    values.remove(curPos + 1);
                    values.remove(curPos + 1);
                    operators.remove(curPos);
                    curPos = -1;
                    break;
                case '÷':
                case '/':
                    values.add(curPos, values.get(curPos) / values.get(curPos + 1));
                    values.remove(curPos + 1);
                    values.remove(curPos + 1);
                    operators.remove(curPos);
                    curPos = -1;
                    break;
                default:
                    break;
            }
        }
        for (curPos = 0; curPos <= operators.size() - 1; curPos++) {
            op = operators.get(curPos).charAt(0);
            switch (op) {
                case '+':
                    values.add(curPos, values.get(curPos) + values.get(curPos + 1));
                    values.remove(curPos + 1);
                    values.remove(curPos + 1);
                    operators.remove(curPos);
                    curPos = -1;
                    break;
                case '-':
                    values.add(curPos, values.get(curPos) - values.get(curPos + 1));
                    values.remove(curPos + 1);
                    values.remove(curPos + 1);
                    operators.remove(curPos);
                    curPos = -1;
                    break;
                default:
                    break;
            }
        }
        return values.get(0);
    }
}
