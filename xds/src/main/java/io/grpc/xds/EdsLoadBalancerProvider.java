/*
 * Copyright 2020 The gRPC Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.grpc.xds;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.grpc.xds.XdsLbPolicies.WEIGHTED_TARGET_POLICY_NAME;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import io.grpc.Internal;
import io.grpc.LoadBalancer;
import io.grpc.LoadBalancer.Helper;
import io.grpc.LoadBalancerProvider;
import io.grpc.LoadBalancerRegistry;
import io.grpc.NameResolver.ConfigOrError;
import io.grpc.internal.ServiceConfigUtil.PolicySelection;
import java.util.Map;
import javax.annotation.Nullable;

/**
 * The provider for the "eds" balancing policy.  This class should not be directly referenced in
 * code.  The policy should be accessed through {@link io.grpc.LoadBalancerRegistry#getProvider}
 * with the name "eds_experimental").
 */
@Internal
public class EdsLoadBalancerProvider extends LoadBalancerProvider {

  @Override
  public boolean isAvailable() {
    return true;
  }

  @Override
  public int getPriority() {
    return 5;
  }

  @Override
  public String getPolicyName() {
    return XdsLbPolicies.EDS_POLICY_NAME;
  }

  @Override
  public LoadBalancer newLoadBalancer(Helper helper) {
    return new EdsLoadBalancer(helper);
  }

  @Override
  public ConfigOrError parseLoadBalancingPolicyConfig(
      Map<String, ?> rawLoadBalancingPolicyConfig) {
    throw new UnsupportedOperationException("not supported as top-level LB policy");
  }

  static final class EdsConfig {
    final String clusterName;
    @Nullable
    final String edsServiceName;
    @Nullable
    final String lrsServerName;
    final PolicySelection localityPickingPolicy;
    final PolicySelection endpointPickingPolicy;

    // TODO(chengyuanzhang): delete me.
    EdsConfig(
        String clusterName,
        @Nullable String edsServiceName,
        @Nullable String lrsServerName,
        PolicySelection endpointPickingPolicy) {
      this.clusterName = checkNotNull(clusterName, "clusterName");
      this.edsServiceName = edsServiceName;
      this.lrsServerName = lrsServerName;
      this.endpointPickingPolicy = checkNotNull(endpointPickingPolicy, "endpointPickingPolicy");
      LoadBalancerProvider provider =
          LoadBalancerRegistry.getDefaultRegistry().getProvider(WEIGHTED_TARGET_POLICY_NAME);
      localityPickingPolicy = new PolicySelection(provider, null, null);
    }

    EdsConfig(
        String clusterName,
        @Nullable String edsServiceName,
        @Nullable String lrsServerName,
        PolicySelection localityPickingPolicy,
        PolicySelection endpointPickingPolicy) {
      this.clusterName = checkNotNull(clusterName, "clusterName");
      this.edsServiceName = edsServiceName;
      this.lrsServerName = lrsServerName;
      this.localityPickingPolicy = checkNotNull(localityPickingPolicy, "localityPickingPolicy");
      this.endpointPickingPolicy = checkNotNull(endpointPickingPolicy, "endpointPickingPolicy");
    }

    @Override
    public String toString() {
      return MoreObjects.toStringHelper(this)
          .add("clusterName", clusterName)
          .add("edsServiceName", edsServiceName)
          .add("lrsServerName", lrsServerName)
          .add("localityPickingPolicy", localityPickingPolicy)
          .add("endpointPickingPolicy", endpointPickingPolicy)
          .toString();
    }

    @Override
    public boolean equals(Object obj) {
      if (!(obj instanceof EdsConfig)) {
        return false;
      }
      EdsConfig that = (EdsConfig) obj;
      return Objects.equal(this.clusterName, that.clusterName)
          && Objects.equal(this.edsServiceName, that.edsServiceName)
          && Objects.equal(this.lrsServerName, that.lrsServerName)
          && Objects.equal(this.localityPickingPolicy, that.localityPickingPolicy)
          && Objects.equal(this.endpointPickingPolicy, that.endpointPickingPolicy);
    }

    @Override
    public int hashCode() {
      return
          Objects.hashCode(
              clusterName,
              edsServiceName,
              lrsServerName,
              localityPickingPolicy,
              endpointPickingPolicy);
    }
  }
}
