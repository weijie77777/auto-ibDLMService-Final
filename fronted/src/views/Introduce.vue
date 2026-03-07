<template>
  <div class="container">
    <!--左侧导航栏-->
    <div class="navbar">
      <ul class="tab-title">
        <router-link to="/">
          <img src="@/assets/images/logo1.png" alt="">
        </router-link>

        <div class="first">
          <router-link to="/">
            <span>auto-ibDLM Detailed Explanation</span>
          </router-link>
        </div>

        <div class="second">
          <span>CONTENTS:</span>
        </div>
        <li 
          v-for="(item, index) in tabs" 
          :key="index"
          :class="{ active: activeTab === index }"
          @click="switchTab(index)"
        >
          {{ item.title }}
        </li>
      </ul>
    </div>
    
    <!--正文-->
    <div class="tab-content">
      <!-- Tab 1: Introduce -->
      <div class="tab-panel" :class="{ show: activeTab === 0 }">
        <div class="text">
          <p>Welcome to auto-ibDLM!</p>
          <a href=""><img src="@/assets/images/img_3.png" alt=""></a>
          <a href=""> <img src="@/assets/images/img_4.png" alt=""></a>
          <div class="text1">
            The core of the auto-ibDLM is a hybrid representation learning
            strategy that first encodes network evolution into interpretable,
            multi-level structural features and then maps them into a compact and 
            robust latent space through a dedicated auto-learning layer. This design 
            effectively captures evolutionary patterns from limited temporal observations 
            while maintaining model explainability, which is essential for trustworthy 
            service intelligence. The learned representations are subsequently integrated 
            into a GRU-based temporal prediction module to forecast future participant
            increments, providing actionable signals for service planning and intervention.
            <br>
            <br>
            The overall architecture of auto-ibDLM is shown as follows:
            <br>
            <br>
            <img src="../assets/images/model.png" alt="auto-ibDLM" style="width: 90%; margin: 0 auto;"/>
            <br>
            <br>
            The framework follows a four-stage pipeline for forecasting
            the development of public events. Given an event, Step 1
            models its evolution as a dynamic interaction network derived 
            from participants and their interaction behaviors. The
            prediction of whether the event will escalate into a public
            event is formalized as the task of forecasting future node
            increments in this network. Step 2 encodes the
            temporal evolution of the dynamic network into multi-level
            feature vectors. Step 3 transforms these
            features into compact and robust latent representations through
            an auto-learning layer. Step 4 employs a GRU-
            based prediction module to estimate future node increments. 
            Steps 2 and 3 jointly form 
            our hybrid representation learning module, which processes 
            manually designed features through the auto-learning layer 
            to obtain compact, robust, and informative latent representations 
            that captures evolutionary patterns from limited temporal sequences 
            while retaining interpretability.
          </div>
          <div class="text2">
            CONTENTS:
            <ul>
              <li>
                <a href="javascript:void(0)" @click="switchTab(1)">
                  <span>Problem modeling</span>
                </a>
              </li>
              <li>
                <a href="javascript:void(0)" @click="switchTab(2)">
                  <span>Characterization of Dynamic Interaction Network</span>
                </a>
              </li>
              <li>
                <a href="javascript:void(0)" @click="switchTab(3)">
                  <span>Auto-learning Representation</span>
                </a>
              </li>
              <li>
                <a href="javascript:void(0)" @click="switchTab(4)">
                  <span>GRU-based prediction module</span>
                </a>
              </li>
            </ul>
          </div>
          <div class="footer">
            <hr>
            <div>© 2020-2024, DataNET Group, ChengDu University of Technology.</div>
          </div>
        </div>
      </div>

      <!-- Tab 2: Problem modeling! -->
      <div class="tab-panel" :class="{ show: activeTab === 1 }">
        <div class="textp2">
          <p>Problem modeling!</p>
          <a href=""><img src="@/assets/images/img_3.png" alt=""></a>
          <a href=""> <img src="@/assets/images/img_4.png" alt=""></a>
          <div class="text1"> 
              We conceptualize the development of an event as the
              evolution of a complex system formed by participants and their
              interaction behaviors. This process is modeled as a dynamic
              interaction network.
              <br>
              <br>
            In modeling the dynamic interaction network, each participant 
            is abstracted as a node, and their commenting behaviors are 
            represented as edges connecting the nodes. Specifically, 
            for an event denoted by , the corresponding 
            comment-based interaction network is formulated as , 
            where denotes the set of nodes—that is, 
            participants who posted comments—and 
            signifies the set of edges, capturing commenting 
            interactions among participants 
            (e.g., an edge emerges between two newly involved participants 
            and if they interact via comments). To analyze the temporal 
            evolution of the event more accurately, the entire 
            development process is partitioned into a sequence of 
            time slices, denoted by . For clarity, 
            an illustrative example is provided as follows.
            <br>
            <br>
            <br>
            <img src="../assets/images/event-graph.png" alt="auto-ibDLM" style="width: 90%; margin: 0 auto;"/>
            <br>
            <br>
            <br>
            For illustration, a theater posts an announcement on Sina Weibo 
            that a play will begin in ten hours, initiating an event e′. 
            This event is observed hourly. on the left are the interaction behaviors 
            of participants regarding the event. Within a time step, five users participated in the discussion, resulting in three
            interactions. As the event progressed, within the time step, two more users joined the
            discussion, leading to four new interactions. By the next time step, two additional users joined,
            also resulting in four interaction behaviors. On the right is the dynamic interaction network
            derived from these interactions. In this network, each node represents a user, and each edge
            represents an interaction between users. Particularly noteworthy is that each edge is associated
            with a temporal attribute indicating the time step which the interaction occurred.
            <br>
            <br>
            Our objective is to forecast the node increment of the 
            dynamic event-relevant network at time step t+1 by 
            leveraging the network slices from time step t and 
            its preceding k−1 time steps.
          </div>
          <div class="footer">
            <hr>
            <div>© 2020-2024, DataNET Group, ChengDu University of Technology.</div>
          </div>
        </div>
      </div>

      <!-- Tab 3: Characterization of Dynamic Interaction Network! -->
      <div class="tab-panel" :class="{ show: activeTab === 2 }">
        <div class="textp3">
          <p>Characterization of Dynamic Interaction Network!</p>
          <a href=""><img src="@/assets/images/img_3.png" alt=""></a>
          <a href=""> <img src="@/assets/images/img_4.png" alt=""></a>
          <div class="text1">
            Predicting the future node increment from historical
            network snapshots requires an effective encoding of each
            snapshot into a feature vector. Although GNNs are widely used
            for graph representation, they often encounter computational
            and memory limitations on large-scale networks, and end-to-
            end embeddings of entire graphs remain difficult to interpret.
            To mitigate these limitations, we encode each snapshot using
            manually defined structural features constructed from network-
            level and node-level metrics. Rather than using raw feature
            values, we model the temporal changes of these structural
            features across consecutive snapshots. These temporal vari-
            ations, combined with community evolution behaviors, form a
            sequential representation of the network’s dynamics and serves
            as the input for forecasting.
            <br>
            <br>
            <h2>Network-level structural features</h2>
            <br>
            <br>
            Given a snapshot of the
            dynamic interaction network, we quantify its network-level
            structural properties using three standard metrics: diameter,
            average shortest path, and density. accordingly, we can construct
            a three-dimensional vector to represent the network-level
            structural features of a snapshot.
            <br>
            <br>
            <h2>Node-level structural features</h2>
            <br>
            <br>
            We identify 11 node-level
            structural metrics: D, EXTD, ACCD, NM, CE, DE, LCC, COREDC, COREDJ, COREDP, COREDPA. Because the dynamic
            interaction network is directed, each metric is computed in
            both its ”in” and ”out” forms. Given the typically large number
            of nodes, directly incorporating per-node metric values into the
            feature vector is impractical and introduces substantial noise.
            Thus, we compute the mean and standard deviation of each
            node-level metric across all nodes, denoted as ”Mean” and
            ”Std”, respectively. These aggregated statistics summarize the
            node-level structural features of each snapshot. Consequently,
            each snapshot yields a 44-dimensional feature vector (11
            metrics × 2 directions × 2 statistics).
            <br>
            <br>
            <h2>Community evolution behaviors</h2>
            <br>
            <br>
            Besides changes in structural features, community 
            evolution behaviors also reflect how the network evolves. 
            In general, seven types of community evolution behaviors 
            are recognized: Birth, Maintain, Dissolve, Merge, Split, Grow, 
            and Shrink. We exclude Birth because it depends on communities 
            in the next snapshot, which are not yet observable. To quantify 
            the remaining six behaviors, we perform community detection on 
            each snapshot of the dynamic interaction network and match communities 
            across consecutive snapshots to identify the evolution behavior of 
            each community and the corresponding evolution degree. For each 
            evolution behavior, we count the number of communities exhibiting that
            behavior and sum their evolution degrees, resulting in two
            values per behavior. Thus, for snapshot, the community
            evolution behaviors occurring between time steps i and i + 1
            are represented by a 12-dimensional vector.
            <br>
            <br>
            <h2>Constructing Feature vector</h2>
            <br>
            <br>
            To characterize the evolution of dynamic interaction network from
            time step i to time step i+1, we compute the difference vectors
            of network-level and node level structural features and concatenate 
            them with the community evolution vector as
            the feature representation.
            <br>
            <br>
          </div>
          <div class="footer">
            <hr>
            <div>© 2020-2024, DataNET Group, ChengDu University of Technology.</div>
          </div>
        </div>
      </div>

      <!-- Tab 4: Auto-learning Representation -->
      <div class="tab-panel" :class="{ show: activeTab === 3 }">
        <div class="textp4">
          <p>Auto-learning Representation!</p>
          <a href=""><img src="@/assets/images/img_3.png" alt=""></a>
          <a href=""> <img src="@/assets/images/img_4.png" alt=""></a>
          <div class="text1">
            <span class="span1">
              <br>
              After encoding the evolution of
              the dynamic interaction network into a sequence of feature
              vectors, one straightforward approach is to feed these vectors
              directly into a sequence model such as an recurrent neural
              network (RNN). However, empirical observations and analysis 
              of several public events on the Sina Weibo platform reveals 
              that different features exhibit markedly different temporal 
              behaviors with respect to node growth. The coexistence of aligned 
              and misaligned patterns highlights the necessity of suppressing noisy 
              or irrelevant feature components. we introduce an auto-learning
              layer implemented as a fully connected transformation. Its goal
              is to convert raw feature vectors into a compact, robust, and
              informative latent representation by automatically reducing the
              influence of non-informative features under varying temporal
              contexts. To ensure that the latent representation preserves informative temporal
              patterns while filtering out noise, we design the following loss
              function:
              <br>
              <img src="../assets/images/lossf.png" alt="auto-ibDLM" style="width: 50%" align="center" />
              <br>
              <br>
              where T is the number of observed snapshots and T − k − 2
              is the number of effective samples under the setting that the
              most recent k snapshots are used for prediction. z is the latent 
              representation of the input sequence, y is the node increments 
              of the input sequence. Minimizing this function can suppress
              inconsistent or chaotic feature dynamics in the latent space,
              thereby improving the model’s ability to predict future node
              increments.
              <br>
            </span>
          </div>
          <div class="footer">
            <hr>
            <div>© 2020-2024, DataNET Group, ChengDu University of Technology.</div>
          </div>
        </div>
      </div>

      <!-- Tab 5: GRU-based prediction module -->
      <div class="tab-panel" :class="{ show: activeTab === 4 }">
        <div class="textp5">
          <p>GRU-based prediction module!</p>
          <a href=""><img src="@/assets/images/img_3.png" alt=""></a>
          <a href=""> <img src="@/assets/images/img_4.png" alt=""></a>
          <div class="text1">
            <h1>The structure of the GRU model</h1>
            <span class="span1">
              <br>
              GRU is an improvement over LSTM and both belong to the RNN category, suitable for
              solving time series problems. In addition to addressing long-term memory and
              vanishing gradient issues, compared to LSTM, GRU has a smaller parameter size and
              higher computational efficiency. Figure 5-1 illustrates the internal structure of
              a GRU unit, which mainly consists of an update gate, reset gate, candidate hidden
              state, and hidden state.
              <br>
            </span>
            <img src="@/assets/images/GRU.png" alt="GRU" style="width: 90%; margin: 0 auto;">
          </div>
          <div class="text2">
            <br>
            <h1>Prediction</h1>
            <br>
            <span>
              After obtaining the latent representations, we construct a
              deep learning module based on a GRU to generate predictive
              outcomes. The model contains k − 1 stacked GRU
              layers, and the output of the final GRU layer is mapped to the
              predicted value through a fully connected layer. When the number of
              predicted value reaches a certain threshold, it is considered that a public event will occur.
            </span>
          </div>
          <div class="footer">
            <hr>
            <div>© 2020-2024, DataNET Group, ChengDu University of Technology.</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';

const activeTab = ref(0);

const tabs = ref([
  { title: 'Introduction' },
  { title: 'Problem modeling' },
  { title: 'Characterization of Dynamic Interaction Network' },
  { title: 'Auto-learning Representation' },
  { title: 'GRU-based prediction module' }
]);

const switchTab = (index) => {
  activeTab.value = index;
  window.scrollTo({ top: 0, behavior: 'smooth' }); 
};
</script>

<style scope>
/* 统一字体 */
* {
  font-family: "Noto Sans SC";
  text-align: justify;
}

a {
  text-decoration: none;
}
.container {
  width: 100%;
  display: flex;          /* 关键：启用弹性布局 */
  flex-direction: row;    /* 横向排列（默认，可省略） */
}
/*侧边导航栏*/
.navbar {
  /* float: left; */
  position: fixed;
  height: 100%;
  width: 30%;
  /* left: 0; */
}

.navbar ul {
  list-style: none;
  width: 100%;
  height: 100vh;
  background-color: #F6F6F6;
}
ul {
    display: block;
    list-style-type: disc;
    margin-block-start: 0;
    margin-block-end: 0;
    padding-inline-start: 0;
    unicode-bidi: isolate;
}

.navbar ul a img {
  width: 200px;
  height: 200px;
  margin-left: 100px;
  margin-top: 20px;
}

.navbar ul li {
  height: 30px;
  margin-top: 10px;
  cursor: pointer;
  text-align: center;
  color: #00a4ff;
}

ul.tab-title li:hover,
ul.tab-title li.active {
  color: red;
}

.first a span {
  font-size: 20px;
  color: black;
  display: flex;
  justify-content: center;
  align-items: center;
  flex-direction: column;
  margin-top: 20px;
}

.second span {
  font-size: 15px;
  color: black;
  display: flex;
  justify-content: center;
  align-items: center;
  flex-direction: column;
  margin-top: 50px;
}

/*右侧数据*/
.tab-content { 
  /* float: left; */
  width: 65%;
  margin-left: 32%;
}
.tab-content .tab-panel {
  display: none;
  justify-content: center;
  align-items: center;
  /* margin-left: 35%; */
  /* width: 800px; */
}

.tab-panel.show {
  display: flex;
}

/* .text {
  float: left;
  width: 800px;
} */

.text p {
  font-size: 35px;
  font-weight: bold;
  color: black;
}

.text .text1 {
  padding-top: 50px;
  font-size: 18px;
  font-weight: lighter;
  line-height: 22px;
  color: black;
}

.text .text2 {
  padding-top: 20px;
}

/* 恢复目录前的黑点和圆圈（覆盖全局 li 重置） */
.text .text2 ul {
  list-style-type: disc;
  padding-left: 20px;
  margin-left: 0;
}

.text .text2 ul > li {
  list-style-type: disc !important;
}

.text .text2 ul li ul {
  list-style-type: circle;
  margin-left: 20px;
}

.text .text2 ul li ul > li {
  list-style-type: circle !important;
}

.text .text2 span {
  font-size: 20px;
  color: #00a4ff;
}

.textp2 {
  float: left;
  width: 800px;
  height: 1200px;
}

.textp2 p {
  font-size: 35px;
  font-weight: bold;
  color: black;
}

.textp2 h1 {
  margin-top: 20px;
  font-size: 25px;
  font-weight: bold;
  color: black;
}

.textp2 .text1 {
  margin-top: 20px;
  font-size: 18px;
  font-weight: lighter;
  line-height: 20px;
  color: black;
}

.textp2 .text2 {
  height: 300px;
}

.textp2 .text3 h1 {
  margin-top: 170px;
}

.textp2 .text3 div {
  margin-top: 20px;
  font-size: 18px;
  font-weight: lighter;
  line-height: 20px;
  color: black;
}

.textp2 .text4 h1 {
  margin-top: 20px;
}

.textp2 .text4 div {
  margin-top: 20px;
  font-size: 18px;
  font-weight: lighter;
  line-height: 20px;
  color: black;
}

.textp3 {
  float: left;
  width: 800px;
}

.textp3 p {
  font-size: 35px;
  font-weight: bold;
  color: black;
}

.textp3 .text1 {
  margin-top: 20px;
  font-size: 18px;
  font-weight: lighter;
  line-height: 20px;
  color: black;
}

.textp3 .photo {
  margin-top: 20px;
}

.textp3 .photo .img1 {
  margin-left: 150px;
}

.textp3 .photo .img2 {
  margin-left: 20px;
}

.textp3 .photo span {
  margin-left: 330px;
  color: red;
}

.textp3 .text2 {
  margin-top: 20px;
  font-size: 18px;
  font-weight: lighter;
  line-height: 20px;
  color: black;
}

.textp4 {
  float: left;
  width: 800px;
}

.textp4 p {
  font-size: 35px;
  font-weight: bold;
  color: black;
}

.textp4 .text1 h1 {
  margin-top: 20px;
  font-size: 25px;
  font-weight: bold;
  color: black;
}

.textp4 .text1 .span1 {
  font-size: 18px;
  font-weight: lighter;
  line-height: 20px;
  color: black;
}

.textp4 .text1 img {
  margin-left: 250px;
}

.textp4 .text1 .span2 {
  margin-left: 350px;
  color: red;
}

.textp4 .text2 h1 {
  margin-top: 20px;
  font-size: 25px;
  font-weight: bold;
  color: black;
}

.textp4 .text2 .span1 {
  font-size: 18px;
  font-weight: lighter;
  line-height: 20px;
  color: black;
}

.textp4 .text2 img {
  margin-left: 80px;
}

.textp4 .text2 .span2 {
  margin-left: 350px;
  color: red;
}

.textp5 {
  float: left;
  width: 800px;
}

.textp5 p {
  font-size: 35px;
  font-weight: bold;
  color: black;
}

.textp5 .text1 h1 {
  margin-top: 20px;
  font-size: 20px;
  font-weight: bold;
  color: black;
}

.textp5 .text1 span {
  font-size: 18px;
  font-weight: lighter;
  line-height: 20px;
  color: black;
}

.textp5 .text2 h1 {
  margin-top: 20px;
  font-size: 20px;
  font-weight: bold;
  color: black;
}

.textp5 .text2 span {
  font-size: 18px;
  font-weight: lighter;
  line-height: 20px;
  color: black;
}

.textp5 .text3 h1 {
  margin-top: 20px;
  font-size: 20px;
  font-weight: bold;
  color: black;
}

.textp5 .text3 span {
  font-size: 18px;
  font-weight: lighter;
  line-height: 20px;
  color: black;
}

.footer {
  margin-left: -30px;
  margin-top: 20px;
  height: 50px;
}

.footer hr {
  width: 850px;
}

.footer div {
  margin-top: 10px;
  margin-left: 30px;
  font-size: 15px;
  color: #c8c9cc;
}
</style>
