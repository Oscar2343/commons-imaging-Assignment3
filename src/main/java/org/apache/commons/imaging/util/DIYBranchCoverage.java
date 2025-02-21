package org.apache.commons.imaging.util;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

public class DIYBranchCoverage {
    private static int branchCnt = 30;
    private static String resultPath = "write_image_coverage.json";
    private static JSONObject result = new JSONObject();

    public static JSONObject initResult() {
        JSONArray branches = new JSONArray();
        for (int i = 0; i < branchCnt; i++) {
            JSONObject branch = new JSONObject();
            branch.put("index", i);
            branch.put("executed", false);
            branches.put(branch);
        }
        result.put("branches", branches);
        return result;
    }

    public static void trackBranch(int branch) {
        if (branch < 0 || branch >= branchCnt) {
            throw new IllegalArgumentException("Invalid branch index");
        }
        JSONArray branches = result.getJSONArray("branches");
        JSONObject branchObj = branches.getJSONObject(branch);
        branchObj.put("executed", true);
    }

    private static void calculateCoverage() {
        int executedBranches = 0;
        JSONArray branches = result.getJSONArray("branches");
        for (int i = 0; i < branchCnt; i++) {
            if (branches.getJSONObject(i).getBoolean("executed")) {
                executedBranches++;
            }
        }
        result.put("executedBranches", executedBranches);
        result.put("totalBranches", branchCnt);
        result.put("coverage", (double) executedBranches / branchCnt * 100 + "%");
    }

    public static void readResult() {
        try {
            result = new JSONObject(FileUtils.readFileToString(new File(resultPath), "UTF-8"));
        } catch (IOException e) {
            e.printStackTrace();
            initResult();
        }
    }

    public static void writeResult() {
        calculateCoverage();
        try {
            FileUtils.writeStringToFile(new File(resultPath), result.toString(4), "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
