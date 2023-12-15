import java.util.ArrayList;
import java.util.Random;

/*
PSO_GA算法确定任务迁移决策、截止日期优先确定任务排序决策
 */
public class PSO_GA_EDF {
    public ResultSet doPSO_GA_EDF(MigrationDecision[] oriMigrationDecisions,int iteration,int requestNum,SupportingApp[] supportingApps,int[][] severToSever) throws CloneNotSupportedException {
        ResultSet resultSet = new ResultSet("PSO_GA_EDF最早截止日期优先的粒子群遗传");
        Random random = new Random();
        MigrationDecision[] migrationDecisions = new MigrationDecision[oriMigrationDecisions.length];
        MigrationDecision[] pBest = new MigrationDecision[oriMigrationDecisions.length];
        int avThroughput = 0;
        //初始化粒子
        for (int i = 0; i < oriMigrationDecisions.length; i++) {
            migrationDecisions[i] = (MigrationDecision) oriMigrationDecisions[i].clone();
            pBest[i] = (MigrationDecision) oriMigrationDecisions[i].clone();
        }
        //粒子开始迭代
        for (int i = 0; i < iteration; i++) {
            //确定gBest下标,gWorst下标
            int gBest = 0;
            int gWorst = 0;
            for (int j = 0; j < migrationDecisions.length; j++) {
                if (migrationDecisions[gBest].fitness < migrationDecisions[j].fitness) {
                    gBest = j;
                }
                if (migrationDecisions[gWorst].fitness > migrationDecisions[j].fitness) {
                    gWorst = j;
                }
            }
            for (int j = 0; j < migrationDecisions.length; j++) {
                EdgeSever[] edgeSevers = migrationDecisions[j].edgeSevers.clone();
                //与gBest做交配运算
                //记录不同点位,对比每一个维度，将newSever值不同的下标存入diffWithGBest中
                ArrayList<Integer> diffWithGBest = new ArrayList<>();
                for (int k = 0; k < migrationDecisions[j].dimensions.length; k++) {
                    if (migrationDecisions[j].dimensions[k].newServe != migrationDecisions[gBest].dimensions[k].newServe) {
                        diffWithGBest.add(k);
                    }
                }
                //确定交配点位的数量,不超过不同点位的1/2
                int matingNumWithGBest = diffWithGBest.size() / 2;
//                matingNumWithGBest = diffWithGBest.size() * (migrationDecisions[j].fitness - migrationDecisions[gWorst].fitness) / (migrationDecisions[gBest].fitness - migrationDecisions[gWorst].fitness) / 2;
                for (int k = 0; k < matingNumWithGBest; k++) {
                    int selectedNumber = random.nextInt(diffWithGBest.size());
                    Dimension dimension = new Dimension();
                    dimension.preServe = migrationDecisions[gBest].dimensions[diffWithGBest.get(selectedNumber)].preServe;
                    dimension.newServe = migrationDecisions[gBest].dimensions[diffWithGBest.get(selectedNumber)].newServe;
                    dimension.request = (Request) migrationDecisions[gBest].dimensions[diffWithGBest.get(selectedNumber)].request.clone();
                    migrationDecisions[j].dimensions[diffWithGBest.get(selectedNumber)] = dimension;
                    diffWithGBest.remove(selectedNumber);
                }

                //与pBest做交配运算
                //记录不同点位,对比每一个维度，将newSever值不同的下标存入diffWithGBest中
                ArrayList<Integer> diffWithPBest = new ArrayList<>();
                for (int k = 0; k < migrationDecisions[j].dimensions.length; k++) {
                    if (migrationDecisions[j].dimensions[k].newServe != pBest[j].dimensions[k].newServe) {
                        diffWithPBest.add(k);
                    }
                }
                //确定交配点位的数量,不超过不同点位的1/2
                int matingNumberWithPBest;
                matingNumberWithPBest = diffWithPBest.size() / 2;
                for (int k = 0; k < matingNumberWithPBest; k++) {
                    int selectedNumber = random.nextInt(diffWithPBest.size());
                    Dimension dimension = new Dimension();
                    dimension.preServe = pBest[j].dimensions[diffWithPBest.get(selectedNumber)].preServe;
                    dimension.newServe = pBest[j].dimensions[diffWithPBest.get(selectedNumber)].newServe;
                    dimension.request = (Request) pBest[j].dimensions[selectedNumber].request.clone();
                    migrationDecisions[j].dimensions[diffWithPBest.get(selectedNumber)] = dimension;
                    diffWithPBest.remove(selectedNumber);
                }
                migrationDecisions[j].updateSevers(edgeSevers);
                migrationDecisions[j].completeNumInDeadline(supportingApps, severToSever);
                //更新pBest
                if (migrationDecisions[j].fitness > pBest[j].fitness) {
                    pBest[j] = (MigrationDecision) migrationDecisions[j].clone();
                }
            }
            if (i == (iteration - 1)) {
                resultSet.completeNum = migrationDecisions[gBest].fitness;
                migrationDecisions[gBest].cacheHitNum();
                resultSet.hitNum = migrationDecisions[gBest].hitNum;

                //负载均衡
//                for (int x = 0; x < migrationDecisions[gBest].edgeSevers.length; x++) {
//                    migrationDecisions[gBest].edgeSevers[x].setLoading();
//                    System.out.print((int) migrationDecisions[gBest].edgeSevers[x].loading + "    ");
//                }
//                System.out.println();
                for (int x = 0; x < migrationDecisions[gBest].edgeSevers.length; x++) {
//                    System.out.println(migrationDecisions[gBest].edgeSevers[x].executeTime);
                    avThroughput += migrationDecisions[gBest].edgeSevers[x].calThroughput();
                }
                System.out.println("PSOGA吞吐量" + avThroughput );

                //截止时限的离散程度
//                for (int x = 0; x < migrationDecisions[gBest].edgeSevers.length; x++) {
//                    migrationDecisions[gBest].edgeSevers[x].setDeadlineDispersion();
//                    System.out.print((int) migrationDecisions[gBest].edgeSevers[x].deadlineDispersion + "    ");
//                }
//                System.out.println();

            }
        }
        return resultSet;
    }
}


