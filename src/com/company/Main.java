package com.company;

import com.sun.org.apache.xpath.internal.operations.Bool;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

public class Main {

    static int[] infWord = {0, 1, 1, 0};
    static String polynomial = "x3 + x2 + x0";

    public static void main(String[] args) throws IOException {
//        int[][] matrixG = createG();
        int[][] matrixG = {
                {1, 0, 1, 1, 0, 0, 0},
                {1, 1, 1, 0, 1, 0, 0},
                {1, 1, 0, 0, 0, 1, 0},
                {0, 1, 1, 0, 0, 0, 1}
        };
        printMatrix(matrixG, "G");

//        int[][] matL = splitMatrix(matrixG).get(0);
//        int[][] matR = splitMatrix(matrixG).get(1);
//        printMatrix(matL, "matL");
//        printMatrix(matR, "matR");

//        int[] z = multVectorByMatrix(infWord, matrixG);
//        printVector(z, "z");
//        z[3] = 1;

        int[][] matrixH = createH(matrixG);
        printMatrix(matrixH, "H");
        printMatrix(transpose(matrixH), "HT");

//        int[] s = multVectorByMatrix(z, transpose(matrixH));
//        printVector(s, "s");
//
//        if (!isEmpty(s)) {
//            correct(z, s, transpose(matrixH));
//        }

        BufferedImage image = ImageIO.read(new File("/Users/DimKa_N7/Documents/IS/Lab1/self1.png"));

        ArrayList<String[][]> encodedColorMatrix = new ArrayList<>();

//        int[][] imagePixels = getPixels(image, 0);
//        printMatrix(imagePixels, "Pixels Red");
//        String[][] imagePixelsBin = binarizeImage(imagePixels);
//        printMatrix(imagePixelsBin, "Binarized (color" + 0 + ")");
//        String[][] imagePixelsEncoded = encodeMatrix(imagePixelsBin, matrixG, matrixH);
////        encodedColorMatrix.add(imagePixelsEncoded);
//        printMatrix(imagePixelsEncoded, "Encoded binary(color" + 0 + ")");

        for (int c = 0; c < 3; c++) {
            int[][] imagePixels = getPixels(image, c);
            printMatrix(imagePixels, "ImagePixels (color)" + c + ")");
            String[][] imagePixelsBin = binarizeImage(imagePixels);
            printMatrix(imagePixelsBin, "Binarized (color" + c + ")");
            String[][] imagePixelsEncoded = encodeMatrix(imagePixelsBin, matrixG, matrixH);
            encodedColorMatrix.add(imagePixelsEncoded);
            printMatrix(imagePixelsEncoded, "Encoded binary(color" + c + ")");
        }

        saveEncodedImage(encodedColorMatrix);

        ArrayList<String[][]> encodedColorMatrixFromFile = readFromFile();

        decodeAndSaveImage(encodedColorMatrixFromFile);

//        int[][] imagePixelsRed = getPixels(image, 0);
//        int[][] imagePixelsGreen = getPixels(image, 1);
//        int[][] imagePixelsBlue = getPixels(image, 2);
//
//        String[][] imagePixelsRedBin = new String[getRowCount(imagePixelsRed)][getColumnCount(imagePixelsRed)];
//        for (int i = 0; i < getRowCount(imagePixelsRedBin); ++i) {
//            for (int j = 0; j < getColumnCount(imagePixelsRedBin); ++j) {
//                imagePixelsRedBin[i][j] = padPixel(imagePixelsRed[i][j]);
//            }
//        }
//        printMatrix(imagePixelsRedBin, "Binarized");
//
//        String[][] imagePixelsEncoded = encodeMatrix(imagePixelsRedBin, matrixG, matrixH);
//
//        printMatrix(imagePixelsEncoded, "Encoded binary");
    }

    // получение нужной строки из матрицы
    private static int[] getRow(int[][] matrix, int rowIndex) {
        int[] result = new int[getColumnCount(matrix)];
        for (int j = 0; j < getColumnCount(matrix); j++) {
            result[j] = matrix[rowIndex][j];
        }
        return result;
    }

    // поэлементное сравнение векторов
    private static boolean isVectorsEqual(int[] vec1, int[] vec2) {
        for (int i = 0; i < vec1.length; i++) {
            if (vec1[i] != vec2[i]) return false;
        }
        return true;
    }

    // исправление ошибки
    private static int[] correct(int[] codedWord, int[] s, int[][] matrixH) {
        int errorIndex = 0;
        for (int i = 0; i < getRowCount(matrixH); i++) {
            if (isVectorsEqual(getRow(matrixH, i), s)) {
                errorIndex = i;
                break;
            }
        }
        print("Ошибка в " + errorIndex + " бите");
        if (codedWord[errorIndex] == 0) codedWord[errorIndex] = 1;
        else codedWord[errorIndex] = 0;
        printVector(codedWord, "Исправленное слово");
        return codedWord;
    }

    private static void decodeAndSaveImage(ArrayList<String[][]> encoded) {
        ArrayList<int[][]> imagePixels = new ArrayList<>(3);
        for (int c = 0; c < encoded.size(); c++) {
            int[][] temp = new int[50][50];
            for (int i = 0; i < 50; i++) {
                for (int j = 0; j < 50; j++) {
                    // поделили закодированное инф слово на 2 части для его декодирования
                    String binary14 = encoded.get(c)[i][j];
                    String leftPart = binary14.substring(0, 7);
                    String rightPart = binary14.substring(7, 14);

                    // декодируем (отбрасываем первые 3 бита)
                    leftPart = leftPart.substring(3);
                    rightPart = rightPart.substring(3);

                    String decodedBinary = leftPart + rightPart;
                    temp[i][j] = Integer.parseInt(decodedBinary, 2);
                }
            }
            imagePixels.add(c, temp);
        }
        // создаем и сохраняем изображение по полученным декодированным данным
        try {
            BufferedImage image = ImageIO.read(new File("/Users/DimKa_N7/Documents/IS/Lab1/selfDecoded.png"));
            for (int i = 0; i < 50; i++) {
                for (int j = 0; j < 50; j++) {
                    int pixel = (255 << 24) | (imagePixels.get(0)[i][j] << 16) | (imagePixels.get(1)[i][j] << 8) | imagePixels.get(2)[i][j];
                    image.setRGB(j, i, pixel);
                }
            }
            ImageIO.write(image, "png", new File("/Users/DimKa_N7/Documents/IS/Lab1/selfDecoded.png"));
        } catch (Exception e ) { }
    }

    // загрузка закодированного изображения из файла
    private static ArrayList<String[][]> readFromFile() {
        ArrayList<String[][]> result = new ArrayList<>();
        String encodedData = "";
        try {
            encodedData = new String(Files.readAllBytes(Paths.get("/Users/DimKa_N7/Documents/IS/Lab1/encoded.txt")));
            int start = 0;
            int end = 14;
            for (int c = 0; c < 3; c++) {
                String[][] arr = new String[50][50];
                for(int i = 0; i < 50; i++) {
                    for (int j = 0; j < 50; j++) {
                        arr[i][j] = encodedData.substring(start, end);
                        start = end;
                        end += 14;
                    }
                }
                result.add(arr);
            }
        } catch (Exception e) { }
        return result;
    }

    //  перевод значений пикселей в бинарный вид
    private static String[][] binarizeImage(int[][] imagePixels) {
        String[][] result = new String[getRowCount(imagePixels)][getColumnCount(imagePixels)];
        for (int i = 0; i < getRowCount(result); i++) {
            for (int j = 0; j < getColumnCount(result); j++) {
                result[i][j] = padPixel(imagePixels[i][j]);
            }
        }
        return result;
    }

    // сохранение закодированного изображения в файл
    private static void saveEncodedImage(ArrayList<String[][]> encodedColorMatrixs) {
        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(new File("/Users/DimKa_N7/Documents/IS/Lab1/encoded.txt"));
            for (int c = 0; c < 3; c++) {
                for (int i = 0; i < 50; i++) {
                    for (int j = 0; j < 50; j++) {
                        fileWriter.write(encodedColorMatrixs.get(c)[i][j]);
                    }
                }
            }
        } catch (Exception e) { }
        finally {
            try {
                fileWriter.close();
            } catch (IOException e) { }
        }
    }

    // кодирование матрицы пикселей изображения при помощи матрицы G и проверка синдромов
    private static String[][] encodeMatrix(String[][] imagePixelsBin, int[][] matrixG, int[][] matrixH) {
        String[][] result = new String[getRowCount(imagePixelsBin)][getColumnCount(imagePixelsBin)];
        for (int i = 0; i < getRowCount(imagePixelsBin); i++) {
            for (int j = 0; j < getColumnCount(imagePixelsBin); j++) {
                int[] infWord = stringToIntArr(imagePixelsBin[i][j]);
                int[] leftPart = splitInfVector(infWord).get(0);
                int[] rightPart = splitInfVector(infWord).get(1);

                int[] zLeft = multVectorByMatrix(leftPart, matrixG);
                int[] zRight = multVectorByMatrix(rightPart, matrixG);

                int[] s_zLeft = multVectorByMatrix(zLeft, transpose(matrixH));
                int[] s_zRight = multVectorByMatrix(zRight, transpose(matrixH));

                if (!isEmpty(s_zLeft) || !isEmpty(s_zRight)) {
                    if (!isEmpty(s_zLeft)) zLeft = correct(zLeft, s_zLeft, transpose(matrixH));
                    if (!isEmpty(s_zRight)) zRight = correct(zRight, s_zRight, transpose(matrixH));
                    break;
                }

                String encodedInfWord = intArrToString(zLeft) + intArrToString(zRight);
                result[i][j] = encodedInfWord;
            }
        }
        return result;
    }

    // проверка вектора на наличие в нем 1
    private static boolean isEmpty(int[] vector) {
        for (int i = 0; i < vector.length; i++) {
            if (vector[i] == 1) return false;
        }
        return true;
    }

    // добавление пикселей в начало бинарного представления пикселя для того, чтобы длина всегда была 8
    private static String padPixel(int pixel) {
        String binary = Integer.toBinaryString(pixel);
        if (binary.length() < 8) {
            for (int i = binary.length(); i < 8; i++) {
                binary = "0" + binary;
            }
        }
        return binary;
    }

    private static int[] stringToIntArr(String vector) {
        int[] result = new int[vector.length()];
        for (int i = 0; i < vector.length(); i++) {
            result[i] = Character.getNumericValue(vector.charAt(i));
        }
        return result;
    }

    private static String intArrToString(int[] arr) {
        String result = "";
        for (int i = 0; i < arr.length; i++) {
            result += Integer.toString(arr[i]);
        }
        return result;
    }

    // разделение информационного слова на 2 части по 4 бита
    private static ArrayList<int[]> splitInfVector(int[] vector) {
        // первый элемент - 4 левых бита, второй - 4 правых
        ArrayList<int[]> result = new ArrayList<>();
        int[] leftPart = new int[4];
        int[] rightPart = new int[4];
        for (int i = 0; i < vector.length; i++) {
            if (i < 4) {
                leftPart[i] = vector[i];
            }
            else {
                rightPart[i - 4] = vector[i];
            }
        }
        result.add(leftPart);
        result.add(rightPart);
        return result;
    }

    // alpha - 0 red - 1 green - 2 blue - 3
    private static int[][] getPixels(BufferedImage image, int color) {
        int width = image.getWidth();
        int height = image.getHeight();
        int[][] result = new int[height][width];
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                Color c = new Color(image.getRGB(col, row));
                switch (color) {
                    case 0:
                        result[row][col] = c.getRed();
                        break;
                    case 1:
                        result[row][col] = c.getGreen();
                        break;
                    case 2:
                        result[row][col] = c.getBlue();
                        break;
                }
            }
        }
//        printMatrix(result, "Image matrix");
        return result;
    }

    // создание матрицы H
    private static int[][] createH(int[][] matrixG) {
        int[][] matL = splitMatrix(matrixG).get(0);
        matL = transpose(matL);
        int[][] result = new int[3][7];
        for (int i = 0; i < getRowCount(result); i++) {
            for (int j = 0; j < getColumnCount(result); j++) {
                if (j < getRowCount(result) && i == j) {
                    result[i][j] = 1;
                }
                else if (j >= getRowCount(result)) {
                    result[i][j] = matL[i][j - getRowCount(result)];
                }
            }
        }
        return result;
    }

    private static int[][] transpose(int[][] matrix) {
        int[][] result = new int[getColumnCount(matrix)][getRowCount(matrix)];
        for (int i = 0; i < getRowCount(matrix); i++) {
            for (int j = 0; j < getColumnCount(matrix); j++) {
                result[j][i] = matrix[i][j];
            }
        }
        return result;
    }

    private static int[] multVectorByMatrix(int[] vector, int[][] matrix) {
        // mxn * nxk = mxk
        int[] result = new int[getColumnCount(matrix)];
        for (int j = 0; j < getColumnCount(matrix); j++) {
            int sum = 0;
            for (int i = 0; i < vector.length; i++) {
                sum = sumMod2(sum, multMod2(vector[i], matrix[i][j]));
            }
            result[j] = sum;
        }
        return result;
    }

    private static int multMod2(int a, int b) {
        boolean a_b = a != 0;
        boolean b_b = b != 0;
        boolean result = a_b && b_b;
        if (result) return 1;
        else return 0;
    }

    private static int sumMod2(int a, int b) {
        return (a + b) % 2 == 0 ? 0 : 1;
    }

    // разделение матрицы на 2 части. Для удобства нахождения H
    private static ArrayList<int[][]> splitMatrix(int[][] matrix) {
        // первое возвращаемое значение - левая часть матрицы, второе - правая
        ArrayList<int[][]> result = new ArrayList<>();
        int[][] matL = new int[4][3];
        int[][] matR = new int[4][4];
        for (int i = 0; i < getRowCount(matrix); i++) {
            for (int j = 0; j < getColumnCount(matrix); j++) {
                if (j < 3) {
                    matL[i][j] = matrix[i][j];
                }
                else {
                    matR[i][j - 3] = matrix[i][j];
                }
            }
        }
        result.add(matL);
        result.add(matR);
        return result;
    }

    private static void printMatrix(int[][] matrix, String matrixName) {
        print(matrixName + ": ");
        for (int i = 0; i < getRowCount(matrix); i++) {
            for (int j = 0; j < getColumnCount(matrix); j++) {
                System.out.print(matrix[i][j] + "\t");
            }
            System.out.println();
        }
    }

    private static void printMatrix(String[][] matrix, String matrixName) {
        print(matrixName + ": ");
        for (int i = 0; i < getRowCount(matrix); i++) {
            for (int j = 0; j < getColumnCount(matrix); j++) {
                System.out.print(matrix[i][j] + "\t");
            }
            System.out.println();
        }
    }

    private static void printVector(int[] vector, String vectorName) {
        print(vectorName + ": ");
        for (int i = 0; i < vector.length; i++) {
            System.out.print(vector[i] + "\t");
        }
        System.out.println();
    }

    private static void printVector(String[] vector, String vectorName) {
        print(vectorName + ": ");
        for (int i = 0; i < vector.length; i++) {
            System.out.print(vector[i] + "\t");
        }
        System.out.println();
    }

    private static void print(String str) {
        System.out.println(str);
    }

    private static int getRowCount(int[][] matrix) {
        return matrix.length;
    }

    private static int getRowCount(String[][] matrix) {
        return matrix.length;
    }

    private static int getColumnCount(int[][] matrix) {
        return matrix[0].length;
    }

    private static int getColumnCount(String[][] matrix) {
        return matrix[0].length;
    }
}